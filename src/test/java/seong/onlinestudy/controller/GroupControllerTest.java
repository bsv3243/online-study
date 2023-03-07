package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.GroupCategory;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.OrderBy;
import seong.onlinestudy.service.GroupService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
        groupRequest.setCategory(GroupCategory.ETC);

        Member member = MyUtils.createMember("member", "member");

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
    void 그룹생성_바인딩실패() throws Exception {
        //given
        GroupCreateRequest groupRequest = new GroupCreateRequest();
        groupRequest.setName("그룹");
        groupRequest.setHeadcount(50);

        Member member = MyUtils.createMember("mebmer", "member");

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
                .andExpect(rs -> assertThat(rs.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void 그룹생성_유효하지않은세션() throws Exception {
        //given
        GroupCreateRequest groupRequest = new GroupCreateRequest();
        groupRequest.setName("그룹");
        groupRequest.setHeadcount(30);
        groupRequest.setCategory(GroupCategory.ETC);

        session.setAttribute(LOGIN_MEMBER, null);

        given(groupService.createGroup(any(), any())).willReturn(1L);

        //when
        MvcResult mvcResult = mvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(groupRequest)))
                .andExpect(rs -> assertThat(rs.getResolvedException())
                        .isInstanceOf(InvalidSessionException.class))
                .andDo(print())
                .andReturn();

        //then
//        result
//                .andExpect(rs -> assertThat(rs.getResolvedException())
//                        .isInstanceOf(InvalidSessionException.class));
    }

    @Test
    void getGroups() throws Exception {
        //given
        GroupDto groupDto = new GroupDto();
        groupDto.setGroupId(1L); groupDto.setName("groupA");
        groupDto.setHeadcount(30);
        groupDto.setCategory(GroupCategory.JOB);

        int page = 0; int size=10;
        GroupCategory category = GroupCategory.ETC;
        String search = null;
        List<Long> studyIds = null;
        OrderBy orderBy = null;

        PageImpl<GroupDto> groupDtos = new PageImpl<>(List.of(groupDto), PageRequest.of(page, size), 1);
        given(groupService.getGroups(any())).willReturn(groupDtos);

        //when
        ResultActions actions = mvc.perform(get("/api/v1/groups"));

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.data.size()").value(1))
                .andDo(print());
    }

}