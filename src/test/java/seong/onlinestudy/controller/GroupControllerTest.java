package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.service.GroupService;

import java.net.BindException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.SessionConst.*;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    GroupService groupService;

    ObjectMapper mapper;
    MockHttpSession session;

    @BeforeEach
    void init() {
        mapper = new ObjectMapper();
        session = new MockHttpSession();
    }

    @Test
    void 그룹생성() throws Exception {
        //given
        GroupCreateRequest groupRequest = new GroupCreateRequest();
        groupRequest.setName("그룹이름");
        groupRequest.setHeadcount(30);

        MemberCreateRequest memberRequest = new MemberCreateRequest();
        memberRequest.setUsername("test1234");
        memberRequest.setPassword("test1234");
        memberRequest.setNickname("test");
        Member member = Member.createMember(memberRequest);

        session.setAttribute(LOGIN_MEMBER, member);
        given(groupService.createGroup(groupRequest, member)).willReturn(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(groupRequest)))
                .andDo(print());

        //then
        result
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void 그룹생성_실패() throws Exception {
        //given
        GroupCreateRequest groupRequest = new GroupCreateRequest();
        groupRequest.setName("그룹");
        groupRequest.setHeadcount(50);

        MemberCreateRequest memberRequest = new MemberCreateRequest();
        memberRequest.setUsername("test1234");
        memberRequest.setPassword("test1234");
        memberRequest.setNickname("test");
        Member member = Member.createMember(memberRequest);

        session.setAttribute(LOGIN_MEMBER, member);
        given(groupService.createGroup(groupRequest, member)).willReturn(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(groupRequest)))
                .andDo(print());

        //then
        result
                .andExpect(rs -> assertThat(rs.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class));
    }


}