package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @DisplayName("티켓 일괄 만료 후, 티켓 기록 일괄 생성")
    void batchInsertUsingJdbcTemplate() {
        //given
        Member testMember = createMember("member", "member");
        Group testGroup = createGroup("group", 30, testMember);
        Study testStudy = createStudy("study");

        List<Ticket> testStudyTickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            Ticket studyTicket = StudyTicket.createStudyTicket(testMember, testGroup, testStudy);
            testStudyTickets.add(studyTicket);
        }

        memberRepository.save(testMember);
        groupRepository.save(testGroup);
        studyRepository.save(testStudy);
        ticketRepository.saveAll(testStudyTickets);

        em.flush();

        //when
        ticketRepository.expireTicketsWhereExpiredFalse();
        ticketRecordRepository.insertTicketRecords(testStudyTickets);

        //then
        List<TicketRecord> findTicketRecords = ticketRecordRepository.findAll();

        assertThat(findTicketRecords.size()).isEqualTo(50);
        assertThat(findTicketRecords).allSatisfy(ticketRecord -> {
            assertThat(ticketRecord.getId()).isNotNull();
            assertThat(ticketRecord.getExpiredTime()).isAfterOrEqualTo(ticketRecord.getTicket().getStartTime());
            assertThat(ticketRecord.getActiveTime()).isGreaterThanOrEqualTo(0);
        });
    }
}