package seong.onlinestudy.repository.jdbctemplate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.repository.*;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JdbcTicketRecordRepositoryCustomTest {

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
    public void insertTicketRecords() {
        //given
        List<Member> members = MyUtils.createMembers(0, 5);
        memberRepository.saveAll(members);

        List<Group> groups = MyUtils.createGroups(members, 2);
        groupRepository.saveAll(groups);

        List<Study> studies = MyUtils.createStudies(2);
        studyRepository.saveAll(studies);

        List<Ticket> studyTickets = MyUtils.createStudyTickets(members, groups, studies, false);
        ticketRepository.saveAll(studyTickets);
        em.flush();

        //when
        ticketRecordRepository.insertTicketRecords(studyTickets);

        //then
        List<TicketRecord> result = ticketRecordRepository.findAll();
        Assertions.assertThat(result.size()).isGreaterThan(0);
    }

}