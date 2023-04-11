package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.GroupRole;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@DataJpaTest
class TicketRecordRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    TicketRecordRepository ticketRecordRepository;

    @Test
    void updateRecordsWhereExpiredFalse() {
        //given
        List<Member> members = createMembers(20);
        List<Group> groups = createGroups(members, 2);
        List<Study> studies = createStudies(2);
        for(int i=2; i<20; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i % groups.size()).addGroupMember(groupMember);
        }
        List<Ticket> tickets = new ArrayList<>();
        for(int i=0; i<20; i++) {
            Ticket ticket = createStudyTicket(members.get(i),
                    groups.get(i % groups.size()), studies.get(i % studies.size())
            );

            ReflectionTestUtils.setField(ticket, "startTime", LocalDateTime.now().plusMinutes(i));

            tickets.add(ticket);
        }

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(tickets);

        //when
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        ZoneOffset offset = ZoneOffset.of("+00:00");
        List<Long> studyIds = tickets.stream().map(Ticket::getId).collect(Collectors.toList());
        em.clear();
        em.flush();
        ticketRecordRepository.updateRecordsWhereExpiredFalse(endTime, endTime.toEpochSecond(offset),
                studyIds);

        //then
        Ticket ticket = tickets.stream().findAny().get();
        LocalDateTime startTime = ticket.getStartTime();

        List<TicketRecord> ticketRecords = ticketRecordRepository.findAll();
        ReflectionTestUtils.setField(ticketRecords.get(0), "expiredTime", endTime);

        log.info("활성화된 시간={}", ticketRecords.stream().map(TicketRecord::getActiveTime).collect(Collectors.toList()));

        assertThat(ticketRecords).allSatisfy(record -> {
            LocalDateTime expiredTime = record.getExpiredTime();
            assertLocalDateTimeEquals(endTime, expiredTime);
        });
    }

    private void assertLocalDateTimeEquals(LocalDateTime target, LocalDateTime source) {
        assertThat(source.getYear())
                .isEqualTo(target.getYear());
        assertThat(source.getMonth())
                .isEqualTo(target.getMonth());
        assertThat(source.getDayOfMonth())
                .isEqualTo(target.getDayOfMonth());
        assertThat(source.getHour())
                .isEqualTo(target.getHour());
        assertThat(source.getMinute())
                .isEqualTo(target.getMinute());
        assertThat(source.getSecond())
                .isEqualTo(target.getSecond());
    }
}