package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.domain.TicketStatus.*;

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

    @Test
    void updateTicketStatus_네이티브쿼리테스트() {
        //given
        List<Member> members = createMembers(30);
        memberRepository.saveAll(members);

        Study study = createStudy("study");
        studyRepository.save(study);

        Group group = createGroup("group", 30, members.get(0));
        groupRepository.save(group);

        for(int i=1; i<30; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            group.addGroupMember(groupMember);
        }

        List<Ticket> tickets = new ArrayList<>();
        for (Member member : members) {
            tickets.add(createTicket(STUDY, member, study, group));
        }
        ticketRepository.saveAll(tickets);
        em.clear();

        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        //when
        int count = em
                .createNativeQuery("update Ticket t" +
                        " set t.is_expired = true, t.end_time=:endTime," +
                        " t.active_time=:endTimeSecond-datediff('second', '1970-01-01', t.start_time)" +
                        " where t.is_expired = false")
                .setParameter("endTime", endTime)
                .setParameter("endTimeSecond", endTime.toEpochSecond(ZoneOffset.of("+00:00")))
                .executeUpdate();

        //then
        assertThat(count).isEqualTo(tickets.size());

        List<Ticket> result = ticketRepository.findAll();
        assertThat(result).allSatisfy(ticket -> {
            assertThat(ticket.isExpired()).isEqualTo(true);
            assertThat(ticket.getActiveTime()).isEqualTo(3600);
        });

        log.info("time={}", result.get(0).getStartTime());

    }

    @Test
    void updateTicketStatus() {
        //given
        List<Member> members = createMembers(30);
        memberRepository.saveAll(members);

        Study study = createStudy("study");
        studyRepository.save(study);

        Group group = createGroup("group", 30, members.get(0));
        groupRepository.save(group);

        for(int i=1; i<30; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            group.addGroupMember(groupMember);
        }

        List<Ticket> tickets = new ArrayList<>();
        for (Member member : members) {
            tickets.add(createTicket(STUDY, member, study, group));
        }
        ticketRepository.saveAll(tickets);

        //when
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        int updateCount = ticketRepository.updateTicketStatusToEnd(
                endTime, endTime.toEpochSecond(ZoneOffset.of("+09:00")));
        em.clear(); //벌크 연산 수행 후 영속성 컨텍스트 초기화

        //then
        assertThat(updateCount).isEqualTo(tickets.size());

        tickets = ticketRepository.findAll();
        assertThat(tickets).allSatisfy(ticket -> {
            assertThat(ticket.isExpired()).isEqualTo(true);
            assertThat(ticket.getActiveTime()).isEqualTo(3600);
        });
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
            tickets.add(createTicket(STUDY, members.get(i), study, group));
        }
        ticketRepository.saveAll(tickets);

        List<Ticket> newTickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            Ticket ticket = createTicket(STUDY, members.get(0), study, group);
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
            tickets.add(createTicket(STUDY, members.get(i), study, group));
        }
        ticketRepository.saveAll(tickets);

        List<Ticket> newTickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            Ticket ticket = createTicket(STUDY, members.get(0), study, group);
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

        Ticket ticket = createTicket(STUDY, member, study, group);
        setField(ticket, "isExpired", true);
        Ticket ticket1 = createTicket(STUDY, member, study, group);
        ticketRepository.save(ticket);
        ticketRepository.save(ticket1);

        //when
        Ticket ticket2 = ticketRepository.findByMemberAndIsExpiredFalse(member).get();

        //then
        assertThat(ticket2).isEqualTo(ticket1);

    }
}