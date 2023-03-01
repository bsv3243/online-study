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
import seong.onlinestudy.domain.*;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.createGroup;
import static seong.onlinestudy.MyUtils.createMember;
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
        List<Member> members = new ArrayList<>();
        for(int i=0; i<50; i++) {
            Member member = createMember("testMember" + i, "testMember" + i);
            members.add(member);
        }
        memberRepository.saveAll(members);

        List<Group> groups = new ArrayList<>();
        for(int i=0; i<10; i++) {
            groups.add(createGroup("테스트그룹" + i, 30, members.get(i)));
        }
        groupRepository.saveAll(groups);

        for(int i=10; i<50; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i % 10).addGroupMember(groupMember);
        }

        //when
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("page", "0"); map.add("size", "10");

        ResultActions rs = mvc.perform(get("/api/v1/groups")
                        .params(map));

        //then
        rs
                .andDo(print());
    }
}
