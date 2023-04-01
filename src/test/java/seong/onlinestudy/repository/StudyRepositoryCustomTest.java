package seong.onlinestudy.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupStudyDto;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@DataJpaTest
public class StudyRepositoryCustomTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    TicketRepository ticketRepository;

    List<Member> members;
    List<Group> groups;
    List<Study> studies;
    List<Ticket> studyTickets;


    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        members = createMembers(50);
        groups = createGroups(members, 10);

        joinMembersToGroups(members, groups);

        studies = createStudies(3);
        studyTickets = createStudyTickets(members, groups, studies, false);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(studyTickets);
    }

    @Test
    void findStudies_조건없음() {
        //given
        List<Member> members = createMembers(20);
        List<Group> groups = createGroups(members, 3);
        MyUtils.joinMembersToGroups(members, groups);

        List<Study> studies = createStudies(5);
        List<Ticket> tickets = createStudyTickets(members, groups, studies, false);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(tickets);

        //when
        Long memberId = null;
        Long groupId = null;
        String search = null;
        LocalDateTime startTime = LocalDateTime.now();
        int days = 6;
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Study> result = studyRepository.findStudies(memberId, groupId, search, startTime.minusDays(days), startTime, pageRequest);

        //then
        assertThat(result.getContent()).containsAnyElementsOf(studies);
    }

    @Test
    void findStudies_회원조건() {
        //given
        List<Member> members = createMembers(20);
        List<Group> groups = createGroups(members, 3);
        MyUtils.joinMembersToGroups(members, groups);

        List<Study> studies = createStudies(5);
        List<Ticket> tickets = createStudyTickets(members, groups, studies, false);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(tickets);

        //when
        Long memberId = members.get(0).getId();
        Long groupId = null;
        String search = null;
        LocalDateTime startTime = LocalDateTime.now();
        int days = 6;
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Study> result = studyRepository.findStudies(memberId, groupId, search, startTime.minusDays(days), startTime, pageRequest);

        //then
        List<Ticket> filteredTickets = tickets.stream()
                .filter(ticket ->
                        ticket.getMember().getId().equals(memberId)
                        && ticket instanceof StudyTicket)
                .collect(Collectors.toList());
        List<StudyTicket> testStudyTickets = filteredTickets.stream().map(ticket -> (StudyTicket) ticket).collect(Collectors.toList());
        List<Study> testStudies = testStudyTickets.stream().map(StudyTicket::getStudy).collect(Collectors.toList());

        assertThat(result.getContent()).containsAnyElementsOf(testStudies);
    }

    @Test
    void findStudies_그룹조건() {
        //given
        Group testGroup = groups.get(0);
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        List<Study> testStudiesInGroup = getStudiesInGroup(testGroup);

        //when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Study> studies = studyRepository.findStudies(
                null, testGroup.getId(), null,
                startTime, endTime,
                pageRequest);
        List<Study> findStudies = studies.getContent();

        //then
        assertThat(findStudies).containsExactlyInAnyOrderElementsOf(testStudiesInGroup);
    }

    @Test
    void findStudies_검색어조건() {
        //given
        Study testStudy = createStudy("뷰");
        studyRepository.save(testStudy);

        //when
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Study> findStudiesWithPage = studyRepository.findStudies(null, null, "뷰", startTime, endTime, pageRequest);

        //then
        List<Study> findStudies = findStudiesWithPage.getContent();
        assertThat(findStudies).contains(testStudy);
    }

    @Test
    void findStudiesInGroups() {
        //given
        Group testGroup = groups.get(0);
        List<Group> testGroups = new ArrayList<>();
        testGroups.add(testGroup);

        //when
        List<GroupStudyDto> findGroupStudyDtos = studyRepository.findStudiesInGroups(testGroups);

        //then
        List<Long> findStudyIds = findGroupStudyDtos.stream().map(GroupStudyDto::getStudyId).collect(Collectors.toList());
        List<Long> targetStudyIds = testGroup.getTickets().stream()
                .map(ticket -> {
            StudyTicket studyTicket = (StudyTicket) ticket;
            return studyTicket.getStudy().getId();
        })
                .distinct().
                collect(Collectors.toList());

        assertThat(findStudyIds).containsExactlyInAnyOrderElementsOf(targetStudyIds);


    }

    private List<Study> getStudiesInGroup(Group testGroup) {
        List<Ticket> testStudyTickets = testGroup.getTickets().stream().filter(ticket -> {
            return ticket instanceof StudyTicket;
        }).collect(Collectors.toList());

        return testStudyTickets.stream().map(ticket -> {
            StudyTicket studyTicket1 = (StudyTicket) ticket;
            return studyTicket1.getStudy();
        }).distinct().collect(Collectors.toList());
    }

}
