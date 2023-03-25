package seong.onlinestudy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QTicket.ticket;

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
    List<Ticket> tickets = new ArrayList<>();
    List<Ticket> restTickets = new ArrayList<>();

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        members = MyUtils.createMembers(50);
        groups = MyUtils.createGroups(members, 10);
        studies = MyUtils.createStudies(10);

        for(int i=10; i<50; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i%10).addGroupMember(groupMember);
        }

        for (int i=0; i<50; i++) {
            Ticket ticket = MyUtils.createTicket(TicketStatus.STUDY, members.get(i), studies.get(i % 10), groups.get(i % 10));
            tickets.add(ticket);
        }

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(tickets);

        for(int i=0; i<10; i++) {
            Ticket ticket = MyUtils.createTicket(TicketStatus.REST, members.get(i), studies.get(i % 10), groups.get(i % 10));
            restTickets.add(ticket);
        }
        ticketRepository.saveAll(restTickets);

    }

    @Test
    void 저장데이터확인() {
        List<Group> findGroups = groupRepository.findAll();
        assertThat(findGroups.size()).isEqualTo(10);

        List<Study> findStudies = studyRepository.findAll();
        assertThat(findStudies.size()).isEqualTo(10);
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
        assertThat(findTickets.size()).isEqualTo(tickets.size() + restTickets.size());
        assertThat(findTickets).anySatisfy(findTicket -> {
            assertThat(findTicket.getStudy()).isNull();
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
                = ticketRepository.findTickets(studyId, groupId, memberId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        //then
        List<Study> findStudies = findTickets.stream().map(Ticket::getStudy).collect(Collectors.toList());
        assertThat(findStudies).allSatisfy(studyInner -> studyInner.getId().equals(studyId));

        Set<Study> findStudiesRemoveDuplicate = new HashSet<>(findStudies);
        assertThat(findStudiesRemoveDuplicate.size()).isEqualTo(1);
    }

    @Test
    void findTickets_그룹조건() {
        //given
        Long studyId = null;
        Long groupId = groups.get(0).getId();
        Long memberId = null;

        //when
        List<Ticket> findTickets
                = ticketRepository.findTickets(studyId, groupId, memberId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

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
                = ticketRepository.findTickets(studyId, groupId, memberId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        //then
        List<Member> findMembers = findTickets.stream().map(Ticket::getMember).collect(Collectors.toList());
        assertThat(findMembers).allSatisfy(findMember -> {
            assertThat(findMember).isEqualTo(members.get(0));
        });

        Set<Member> findMembersRemoveDuplicate = new HashSet<>(findMembers);
        assertThat(findMembersRemoveDuplicate.size()).isEqualTo(1);
    }
}
