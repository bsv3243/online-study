package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.GroupRole;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;

    List<Member> members;
    List<Group> groups;
    List<Study> studies;
    List<Ticket> studyTickets;

    @BeforeEach
    void init() {
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
    void updateTicketStatus() {
        //given
        List<Ticket> testTicketsNotExpired = studyTickets.stream()
                .filter(ticket -> !ticket.isExpired())
                .collect(Collectors.toList());

        //when
        int updateCount = ticketRepository.expireTicketsWhereExpiredFalse();
        em.clear(); //벌크 연산 수행 후 영속성 컨텍스트 초기화

        //then
        assertThat(updateCount).isEqualTo(testTicketsNotExpired.size());
    }

    @Test
    void findTickets_단일회원() {
        //given
        List<Member> members = createMembers(50, 70);
        memberRepository.saveAll(members);

        Group group = createGroup("groupA", 30, members.get(0));
        groupRepository.save(group);

        for(int i=1; i<20; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            group.addGroupMember(groupMember);
        }

        Study study = createStudy("studyA");
        studyRepository.save(study);

        List<Ticket> tickets = new ArrayList<>();
        for (int i=0; i<20; i++) {
            tickets.add(createStudyTicket(members.get(i), group, study));
        }
        ticketRepository.saveAll(tickets);

        List<Ticket> newTickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            Ticket ticket = createStudyTicket(members.get(0), group, study);
            setField(ticket, "startTime", LocalDateTime.now().minusDays(5));
            newTickets.add(ticket);
        }
        ticketRepository.saveAll(newTickets);

        //when
        List<Ticket> findTickets = em.createQuery("select t from Ticket t" +
                        " join t.member m on m = :member" +
                        " where t.startTime >= :startTime and t.startTime < :endTime", Ticket.class)
                .setParameter("member", members.get(0))
                .setParameter("startTime", LocalDateTime.now().minusHours(1))
                .setParameter("endTime", LocalDateTime.now().plusSeconds(1))
                .getResultList();

        //then
        assertThat(findTickets.size()).isEqualTo(1);
    }

    @Test
    void findTickets_그룹() {
        //given
        List<Member> members = createMembers(50, 70);
        memberRepository.saveAll(members);
        List<Member> members1 = createMembers(30);
        memberRepository.saveAll(members1);

        Group group = createGroup("groupA", 30, members.get(0));
        groupRepository.save(group);

        for(int i=1; i<20; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            group.addGroupMember(groupMember);
        }

        Study study = createStudy("studyA");
        studyRepository.save(study);

        List<Ticket> tickets = new ArrayList<>();
        for (int i=0; i<20; i++) {
            tickets.add(createStudyTicket(members.get(i), group, study));
        }
        ticketRepository.saveAll(tickets);

        List<Ticket> newTickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            Ticket ticket = createStudyTicket(members.get(0), group, study);
            setField(ticket, "startTime", LocalDateTime.now().minusDays(5));
            newTickets.add(ticket);
        }
        ticketRepository.saveAll(newTickets);

        //when
        List<Ticket> findTickets = em.createQuery("select t from Ticket t" +
                        " join fetch t.member m" +
                        " join m.groupMembers gm on gm.group.id = :groupId" +
                        " where t.startTime >= :startTime and t.startTime < :endTime" +
                        " order by t.member.id", Ticket.class)
                .setParameter("groupId", group.getId())
                .setParameter("startTime", LocalDateTime.now().minusHours(1))
                .setParameter("endTime", LocalDateTime.now().plusSeconds(1))
                .getResultList();

        //then
        assertThat(findTickets.size()).isEqualTo(20);
        assertThat(findTickets).containsExactlyInAnyOrderElementsOf(tickets);
        Set<Member> set = new HashSet<>();
        for (Ticket findTicket : findTickets) {
            set.add(findTicket.getMember());
        }
        assertThat(set).containsExactlyInAnyOrderElementsOf(members);
    }

    @Test
    void findByMemberAndExpiredFalse() {
        Member member = createMember("member", "member");
        memberRepository.save(member);

        Group group = createGroup("group", 30, member);
        groupRepository.save(group);

        Study study = createStudy("study");
        studyRepository.save(study);

        Ticket ticket = createStudyTicket(member, group, study);
        setField(ticket, "expired", true);
        Ticket ticket1 = createStudyTicket(member, group, study);
        ticketRepository.save(ticket);
        ticketRepository.save(ticket1);

        //when
        Ticket ticket2 = ticketRepository.findByMemberAndExpiredFalse(member).get();

        //then
        assertThat(ticket2).isEqualTo(ticket1);

    }
}