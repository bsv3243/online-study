package seong.onlinestudy.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.GroupRole;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class TicketRecordApiTest {

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

    List<Member> members;
    List<Group> groups;
    List<Study> studies;
    List<Ticket> tickets;

    @BeforeEach
    void beforeEach() {
        members = createMembers(50);
        groups = createGroups(members, 10);
        for(int i=10; i<50; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i % 10), GroupRole.USER);
            groups.get(i % 10).addGroupMember(groupMember);
        }
        studies = createStudies(10);
        tickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            tickets.add(createStudyTicket(members.get(i), groups.get(i % 10), studies.get(i % 10)));
        }

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        studyRepository.saveAll(studies);
        ticketRepository.saveAll(tickets);

        for(int i=0; i<20; i++) {
            tickets.get(i).expiredAndUpdateRecord();
        }

    }

    @Test
    void getTickets_조건없음() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/records")
                .params(request));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void getTickets_스터디조건() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("studyId", studies.get(0).getId().toString());

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/records")
                .params(request));

        //then
        MvcResult mvcResult = resultActions.andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void getTickets_그룹조건() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("groupId", groups.get(0).getId().toString());

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/records")
                .params(request));

        //then
        MvcResult mvcResult = resultActions.andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    void getTickets_회원조건() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("memberId", members.get(0).getId().toString());

        //when
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("LOGIN_MEMBER", members.get(0));
        ResultActions resultActions = mvc.perform(get("/api/v1/records")
                .params(request)
                .session(session));

        //then
        MvcResult mvcResult = resultActions.andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }
}
