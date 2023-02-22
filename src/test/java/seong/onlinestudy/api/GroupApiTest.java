package seong.onlinestudy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.controller.GroupController;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class GroupApiTest {

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

    MockHttpSession session;
    ObjectMapper mapper;

    @BeforeEach
    void init() {
        session = new MockHttpSession();
        mapper = new ObjectMapper();

        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("test1234");
        request.setPassword("test1234");
        request.setNickname("test1234");

        Member member = Member.createMember(request);
        memberRepository.save(member);

        session.setAttribute(LOGIN_MEMBER, member);
    }

    @Test
    public void 그룹생성() throws Exception {
        //given
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("테스트그룹");
        request.setHeadcount(30);

        //when
        mvc.perform(post("/api/v1/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .session(session))
                .andDo(print());

        //then
    }

    @Test
    void 그룹조회() throws Exception {
        //given
        Member testMember1 = MyUtils.createMember("testMember1", "testMember1");
        memberRepository.save(testMember1);

        Group testGroup1 = MyUtils.createGroup("테스트그룹1", 30, testMember1);
        groupRepository.save(testGroup1);

        Study testStudy1 = MyUtils.createStudy("테스트스터디1");
        studyRepository.save(testStudy1);

        Ticket testTicket1 = MyUtils.createTicket(testMember1, testStudy1, testGroup1);
        ticketRepository.save(testTicket1);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("page", "0"); map.add("size", "10");
        map.add("category", null); map.add("search", null); map.add("studyIds", null);

        //when
        ResultActions rs = mvc.perform(get("/api/v1/groups")
                .params(map));

        //then
        rs
                .andExpect(status().isOk())
                .andDo(print());
    }
}
