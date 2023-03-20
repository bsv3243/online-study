package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.domain.*;

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
class RecordRepositoryTest {

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
    RecordRepository recordRepository;

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
            Ticket ticket = createTicket(TicketStatus.STUDY, members.get(i),
                    studies.get(i % studies.size()),
                    groups.get(i % groups.size()));

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
//        recordRepository.updateRecordsWhereExpiredFalse(endTime, endTime.toEpochSecond(offset));
        em.clear();
        em.flush();
/*        em.createNativeQuery("update Record r" +
                        " set r.expired_time=:expiredTime," +
                        " r.active_time=:expiredTimeToSeconds - datediff('second', '1970-01-01', (" +
                        "select t.start_time from Ticket t where t.record_id=r.record_id))" +
                        " where r.record_id in (select t.record_id from Ticket t where t.ticket_id in :ticketIds)")
                .setParameter("expiredTime", endTime)
                .setParameter("expiredTimeToSeconds", endTime.toEpochSecond(offset))
                .setParameter("ticketIds", tickets.stream().map(Ticket::getId).collect(Collectors.toList()))
                .executeUpdate();*/
        recordRepository.updateRecordsWhereExpiredFalse(endTime, endTime.toEpochSecond(offset),
                studyIds);

        //then
        Ticket ticket = tickets.stream().findAny().get();
        LocalDateTime startTime = ticket.getStartTime();

        List<Record> records = recordRepository.findAll();
        ReflectionTestUtils.setField(records.get(0), "expiredTime", endTime);

        log.info("활성화된 시간={}", records.stream().map(Record::getActiveTime).collect(Collectors.toList()));

        assertThat(records).allSatisfy(record -> {
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