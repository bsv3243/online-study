package seong.onlinestudy.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.MyUtils.createTicket;
import static seong.onlinestudy.domain.TicketStatus.REST;
import static seong.onlinestudy.domain.TicketStatus.STUDY;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class TicketApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    TicketRepository ticketRepository;

    @BeforeEach
    void beforeEach() {
        List<Member> members = createMembers(10);
        List<Group> groups = createGroups(members, 2);
        for(int i=2; i<10; i++) {
            Group group = groups.get(i % 2);
            group.addGroupMember(GroupMember.createGroupMember(members.get(i), GroupRole.USER));
        }

        List<Study> studies = createStudies(2);
        List<Ticket> studyTicketsToExpire = createTickets(STUDY, members, groups, studies);
        List<Ticket> restTicketsToExpire = createTickets(REST, members, groups, studies);
        List<Ticket> studyTickets = createTickets(STUDY, members, groups, studies);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(studyTicketsToExpire);
        ticketRepository.saveAll(restTicketsToExpire);
        ticketRepository.saveAll(studyTickets);

        MyUtils.expireTickets(studyTicketsToExpire);
        MyUtils.expireTickets(restTicketsToExpire);
    }

    @Test
    void getTickets_withoutCondition() throws Exception {
        //given

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/tickets"));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print());
    }
}
