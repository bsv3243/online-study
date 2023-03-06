package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
    void findMembersWithTickets() {
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
        for (Member member : members) {
            tickets.add(createTicket(STUDY, member, study, group));
        }
        ticketRepository.saveAll(tickets);

        //when
        List<Member> findMembers = ticketRepository.findMembersWithTickets(LocalDate.now().atStartOfDay(),
                LocalDate.now().atStartOfDay().plusDays(1), group.getId());
//        List<Member> findMembers = ticketRepository.findMembersWithTickets(group.getId());

        //then
        List<String> usernames = members.stream().map(Member::getUsername).collect(Collectors.toList());
        List<String> findUsernames = findMembers.stream().map(Member::getUsername).collect(Collectors.toList());

        assertThat(usernames).containsExactlyInAnyOrderElementsOf(findUsernames);

        List<Ticket> findTickets = new ArrayList<>();
        for (Member member : members) {
            findTickets.addAll(member.getTickets());
        }
        assertThat(findTickets).containsExactlyInAnyOrderElementsOf(tickets);
        assertThat(findTickets.size()).isEqualTo(tickets.size());
    }

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
                        " set t.ticket_status='END', t.end_time=:endTime," +
                        " t.active_time=:endTimeSecond-datediff('second', '1970-01-01', t.start_time)" +
                        " where t.ticket_status != 'END'")
                .setParameter("endTime", endTime)
                .setParameter("endTimeSecond", endTime.toEpochSecond(ZoneOffset.of("+00:00")))
                .executeUpdate();

        //then
        assertThat(count).isEqualTo(tickets.size());

        List<Ticket> result = ticketRepository.findAll();
        assertThat(result).allSatisfy(ticket -> {
            assertThat(ticket.getTicketStatus()).isEqualTo(END);
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
            assertThat(ticket.getTicketStatus()).isEqualTo(END);
            assertThat(ticket.getActiveTime()).isEqualTo(3600);
        });
    }
}