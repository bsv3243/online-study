package seong.onlinestudy.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.domain.TicketStatus.STUDY;

@DataJpaTest
class StudyRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    GroupRepository groupRepository;

    @Test
    void findStudiesWithMember() {
        //given
        List<Member> members = createMembers(10);
        memberRepository.saveAll(members);

        List<Study> studies = createStudies(6);
        studyRepository.saveAll(studies);

        Group group = createGroup("group", 30, members.get(0));
        groupRepository.save(group);

        Ticket ticket = createTicket(STUDY, members.get(0), studies.get(4), group);
        ticketRepository.save(ticket);
        setField(ticket, "activeTime", 3600);
        Ticket ticket1 = createTicket(STUDY, members.get(0), studies.get(5), group);
        ticketRepository.save(ticket1);

        List<Ticket> tickets = new ArrayList<>();
        for(int i=1; i<members.size(); i++) {
            GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            tickets.add(createTicket(STUDY, members.get(i), studies.get(i%4), group));
        }
        ticketRepository.saveAll(tickets);


        //when
        LocalDateTime now = LocalDateTime.now();
        /*
        List<Study> findStudies = em.createQuery("select s from Study s" +
                        " join s.tickets t on t.member = :member and t.startTime >= :startTime and t.startTime < :endTime" +
                        " group by s.id" +
                        " order by sum(t.activeTime) desc", Study.class)
                .setParameter("member", members.get(0))
                .setParameter("startTime", now.minusDays(3))
                .setParameter("endTime", now.plusDays(3))
                .getResultList();
         */
        Page<Study> studiesByMember
                = studyRepository.findStudiesByMember(members.get(0), now.minusDays(3), now.plusDays(3), PageRequest.of(0, 2));
        List<Study> findStudies = studiesByMember.getContent();

        //then
        Assertions.assertThat(findStudies).containsExactlyElementsOf(List.of(studies.get(4), studies.get(5)));
    }

}