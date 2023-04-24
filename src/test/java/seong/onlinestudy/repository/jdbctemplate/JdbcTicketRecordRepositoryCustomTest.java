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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static seong.onlinestudy.MyUtils.createMember;
import static seong.onlinestudy.MyUtils.createStudyTicket;

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

        List<Ticket> tickets = new ArrayList<>();
        for(int i=0; i<members.size(); i++) {
            Ticket studyTicket =
                    createStudyTicket(members.get(i), groups.get(i % groups.size()), studies.get(i % studies.size()));
            tickets.add(studyTicket);
        }
        ticketRepository.saveAll(tickets);
        assertThat(tickets).allSatisfy(ticket -> {
            assertThat(ticket.getTicketRecord()).isNull();
        });
        em.flush();
        em.clear();

        //when
        ticketRecordRepository.insertTicketRecords(tickets);

        //then
        tickets = ticketRepository.findAll();
        assertThat(tickets).allSatisfy(ticket -> {
            assertThat(ticket.getTicketRecord()).isNotNull();
        });

    }

}