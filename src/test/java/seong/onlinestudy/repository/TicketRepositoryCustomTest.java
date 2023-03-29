package seong.onlinestudy.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TicketRepositoryCustomTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    TicketRepository ticketRepository;

    //테스트 데이터
    List<Member> members = new ArrayList<>();
    List<Group> groups = new ArrayList<>();
    List<Study> studies = new ArrayList<>();
    List<Ticket> studyTickets = new ArrayList<>();
    List<Ticket> restTickets = new ArrayList<>();

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        members = MyUtils.createMembers(50);
        groups = MyUtils.createGroups(members, 10);
        studies = MyUtils.createStudies(10);

        MyUtils.joinMembersToGroups(members, groups);

        studyTickets = MyUtils.createStudyTickets(members, groups, studies, false);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(this.studyTickets);

        restTickets = MyUtils.createRestTickets(members, groups, false);

        ticketRepository.saveAll(restTickets);

    }

    @Test
    void findTickets_조건없음() {
        //given
        Long studyId = null;
        Long groupId = null;
        Long memberId = null;

        //when
        List<Ticket> findTickets
                = ticketRepository.findTickets(studyId, groupId, memberId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        //then
        assertThat(findTickets.size()).isEqualTo(studyTickets.size() + restTickets.size());
        assertThat(findTickets).anySatisfy(findTicket -> {
            if(findTicket instanceof StudyTicket) {
                StudyTicket studyTicket = (StudyTicket) findTicket;
                assertThat(studyTicket.getStudy()).isNotNull();
            }
        });
    }

    @Test
    void findTickets_스터디조건() {
        //given
        Long studyId = studies.get(0).getId();
        Long groupId = null;
        Long memberId = null;

        //when
        List<Ticket> findTickets
                = ticketRepository.findTickets(memberId, groupId, studyId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        //then
        List<StudyTicket> testStudyTickets = studyTickets.stream()
                .map(ticket -> (StudyTicket) ticket)
                .collect(Collectors.toList());
        List<StudyTicket> testFilteredTickets = testStudyTickets.stream()
                .filter(studyTicket -> studyTicket.getStudy().equals(studies.get(0)))
                .collect(Collectors.toList());

        List<StudyTicket> findStudyTickets = findTickets.stream()
                .map(ticket -> (StudyTicket) ticket)
                .collect(Collectors.toList());

        assertThat(testFilteredTickets).containsExactlyInAnyOrderElementsOf(findStudyTickets);

    }

    @Test
    void findTickets_그룹조건() {
        //given
        Long studyId = null;
        Long groupId = groups.get(0).getId();
        Long memberId = null;

        //when
        List<Ticket> findTickets
                = ticketRepository.findTickets(
                        memberId, groupId, studyId,
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusHours(1)
        );

        //then
        List<Group> findGroups = findTickets.stream().map(Ticket::getGroup).collect(Collectors.toList());
        assertThat(findGroups).allSatisfy(findGroup -> findGroup.getId().equals(groupId));

        Set<Group> findGroupsRemoveDuplicate = new HashSet<>(findGroups);
        assertThat(findGroupsRemoveDuplicate.size()).isEqualTo(1);
    }

    @Test
    void findTickets_멤버조건() {
        //given
        Long studyId = null;
        Long groupId = null;
        Long memberId = members.get(0).getId();

        //when
        List<Ticket> findTickets
                = ticketRepository.findTickets(
                        memberId, groupId, studyId,
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusHours(1)
        );

        //then
        List<Member> findMembers = findTickets.stream().map(Ticket::getMember).collect(Collectors.toList());
        assertThat(findMembers).allSatisfy(findMember -> {
            assertThat(findMember).isEqualTo(members.get(0));
        });

        Set<Member> findMembersRemoveDuplicate = new HashSet<>(findMembers);
        assertThat(findMembersRemoveDuplicate.size()).isEqualTo(1);
    }
}
