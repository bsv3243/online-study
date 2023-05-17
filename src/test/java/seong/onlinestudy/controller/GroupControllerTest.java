package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.enumtype.GroupRole;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.dto.GroupMemberDto;
import seong.onlinestudy.dto.GroupStudyDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.group.GroupCreateRequest;
import seong.onlinestudy.request.group.GroupUpdateRequest;
import seong.onlinestudy.request.group.GroupsDeleteRequest;
import seong.onlinestudy.service.GroupService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static seong.onlinestudy.MyUtils.createMember;
import static seong.onlinestudy.constant.SessionConst.*;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getConstraint;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDefaultValue;
import static seong.onlinestudy.enumtype.GroupCategory.IT;

@AutoConfigureRestDocs
@WebMvcTest(GroupController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(RestDocumentationExtension.class)
class GroupControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    GroupService groupService;

    MockHttpSession session;

    @BeforeEach
    void init(WebApplicationContext context, RestDocumentationContextProvider provider) {
        session = new MockHttpSession();

        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(provider))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    void 그룹생성_바인딩실패() throws Exception {
        //given
        GroupCreateRequest groupRequest = new GroupCreateRequest();
        groupRequest.setName("그룹");
        groupRequest.setHeadcount(50);

        Member member = createMember("mebmer", "member");

        session.setAttribute(LOGIN_MEMBER, 1L);
        given(groupService.createGroup(any(), any())).willReturn(1L);

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

        given(groupService.createGroup(any(), any())).willReturn(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(groupRequest)));

        //then
        result
                .andExpect(rs -> assertThat(rs.getResolvedException()).isInstanceOf(InvalidSessionException.class))
                .andDo(print())
                .andReturn();
    }

    @Test
    @DisplayName("그룹 생성")
    void createGroup() throws Exception {
        //given
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("groupName"); request.setHeadcount(30); request.setCategory(IT);
        Member loginMember = createMember("memberA", "memberPassword");
        session.setAttribute(LOGIN_MEMBER, 1L);

        given(groupService.createGroup(any(), any())).willReturn(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("group-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").type(STRING).attributes(getConstraint("2자 이상, 20자 이하"))
                                        .description("그룹 이름"),
                                fieldWithPath("headcount").type(NUMBER).attributes(getConstraint("1이상, 30이하"))
                                        .description("그룹 인원수"),
                                fieldWithPath("category").type(STRING).description("그룹 카테고리(Enum Type)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("그룹 엔티티 아이디")
                        )));
    }

    @Test
    @DisplayName("그룹 가입")
    public void joinGroup() throws Exception {
        //given
        Member loginMember = createMember("memberA", "memberPassword");
        session.setAttribute(LOGIN_MEMBER, 1L);

        given(groupService.joinGroup(any(), any())).willReturn(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups/{groupId}/join", 1L)
                .session(session));

        //then
        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("group-join",
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("그룹 엔티티 아이디")
                        )));

    }

    @Test
    @DisplayName("그룹 탈퇴")
    public void quitGroup() throws Exception {
        //given
        Member loginMember = createMember("memberA", "memberPassword");
        session.setAttribute(LOGIN_MEMBER, 1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups/{groupId}/quit", 1L)
                .session(session));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("group-quit",
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(STRING).description("메시지")
                        )));
    }

    @Test
    @DisplayName("그룹 목록 조회")
    void getGroups() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("page", "0");
        request.add("size", "10");
        request.add("category", "IT");
        request.add("search", "검색어");
        request.add("studyIds", "1, 2, 3, 4");
        request.add("orderBy", "CREATEDAT");

        Member member = createMember("member", "member");
        Group group = MyUtils.createGroup("group", 30, member);

        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(group, "id", 1L);
        ReflectionTestUtils.setField(group, "description", "그룹 설명");
        ReflectionTestUtils.setField(group, "category", IT);

        GroupDto groupDto = createTestGroupDto(group);

        List<GroupDto> groupDtos = List.of(groupDto);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<GroupDto> value = new PageImpl<>(groupDtos, pageRequest, 1);
        given(groupService.getGroups(any())).willReturn(value);

        //when
        ResultActions result = mvc.perform(get("/api/v1/groups")
                .params(request));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("groups-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("page").attributes(getDefaultValue("0")).
                                        description("페이지"),
                                parameterWithName("size").attributes(getDefaultValue("12"))
                                        .description("응답 데이터 개수"),
                                parameterWithName("memberId").description("회원 엔티티 아이디").optional(),
                                parameterWithName("category").description("그룹 카테고리(Enum Type)").optional(),
                                parameterWithName("search").description("그룹 이름 검색어").optional(),
                                parameterWithName("studyIds").description("스터디 아이디 목록").optional(),
                                parameterWithName("orderBy").attributes(getDefaultValue("CREATEDAT"))
                                        .description("그룹 정렬 순서(Enum Type 탭 참고)")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("name").type(STRING).description("그룹 이름"),
                                fieldWithPath("headcount").type(NUMBER).description("그룹 제한 인원 수"),
                                fieldWithPath("memberSize").type(NUMBER).description("그룹 현제 인원 수"),
                                fieldWithPath("createdAt").type(STRING).description("그룹 생성일"),
                                fieldWithPath("description").type(STRING).description("그룹 설명"),
                                fieldWithPath("category").type(STRING).description("그룹 카테고리"),
                                fieldWithPath("deleted").type(BOOLEAN).description("그룹 삭제여부"),
                                fieldWithPath("groupMembers").type(JsonFieldType.ARRAY).description("그룹원 목록"),
                                fieldWithPath("groupMembers[].groupMemberId").type(NUMBER).description("그룹원 엔티티 아이디"),
                                fieldWithPath("groupMembers[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("groupMembers[].memberId").type(NUMBER).description("회원 엔티티 아이디"),
                                fieldWithPath("groupMembers[].username").type(STRING).description("회원 아이디"),
                                fieldWithPath("groupMembers[].nickname").type(STRING).description("회원 닉네임"),
                                fieldWithPath("groupMembers[].joinedAt").type(STRING).description("회원 그룹 가입일"),
                                fieldWithPath("groupMembers[].role").type(STRING).description("회원 그룹 권한"),
                                fieldWithPath("studies").type(JsonFieldType.ARRAY).description("그룹 스터디 목록"),
                                fieldWithPath("studies[].studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                                fieldWithPath("studies[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("studies[].name").type(STRING).description("스터디 이름"),
                                fieldWithPath("studies[].studyTime").type(NUMBER).description("총 스터디 시간")
                        )));

    }

    @Test
    @DisplayName("그룹 조회")
    void getGroup() throws Exception {
        //given
        Member member = createMember("member", "member");
        Group group = MyUtils.createGroup("group", 30, member);

        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(group, "id", 1L);
        ReflectionTestUtils.setField(group, "description", "그룹 설명");
        ReflectionTestUtils.setField(group, "category", IT);

        GroupDto groupDto = createTestGroupDto(group);

        given(groupService.getGroup(anyLong())).willReturn(groupDto);

        //when
        ResultActions result = mvc.perform(get("/api/v1/groups/{groupId}", 1L));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("group-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 엔티티 아이디")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("name").type(STRING).description("그룹 이름"),
                                fieldWithPath("headcount").type(NUMBER).description("그룹 제한 인원 수"),
                                fieldWithPath("memberSize").type(NUMBER).description("그룹 현제 인원 수"),
                                fieldWithPath("createdAt").type(STRING).description("그룹 생성일"),
                                fieldWithPath("description").type(STRING).description("그룹 설명"),
                                fieldWithPath("category").type(STRING).description("그룹 카테고리"),
                                fieldWithPath("deleted").type(BOOLEAN).description("그룹 사제 여부"),
                                fieldWithPath("groupMembers").type(JsonFieldType.ARRAY).description("그룹원 목록"),
                                fieldWithPath("groupMembers[].groupMemberId").type(NUMBER).description("그룹원 엔티티 아이디"),
                                fieldWithPath("groupMembers[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("groupMembers[].memberId").type(NUMBER).description("회원 엔티티 아이디"),
                                fieldWithPath("groupMembers[].username").type(STRING).description("회원 아이디"),
                                fieldWithPath("groupMembers[].nickname").type(STRING).description("회원 닉네임"),
                                fieldWithPath("groupMembers[].joinedAt").type(STRING).description("회원 그룹 가입일"),
                                fieldWithPath("groupMembers[].role").type(STRING).description("회원 그룹 권한"),
                                fieldWithPath("studies").type(JsonFieldType.ARRAY).description("그룹 스터디 목록"),
                                fieldWithPath("studies[].studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                                fieldWithPath("studies[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("studies[].name").type(STRING).description("스터디 이름"),
                                fieldWithPath("studies[].studyTime").type(NUMBER).description("총 스터디 시간")
                        )));
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup() throws Exception {
        //given
        Member loginMember = createMember("member", "member");

        session.setAttribute(LOGIN_MEMBER, 1L);

        //when
        ResultActions result = mvc.perform(delete("/api/v1/groups/{groupId}", 1L)
                .session(session));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("group-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(STRING).description("짧은 메시지")
                        )));
    }

    @Test
    @DisplayName("그룹 업데이트")
    void updateGroup() throws Exception {
        //given
        Member loginMember = createMember("member", "member");

        session.setAttribute(LOGIN_MEMBER, 1L);

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setDescription("그룹 설명"); request.setHeadcount(30);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups/{groupId}", 1L)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("group-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("그룹 엔티티 아이디")
                        ),
                        requestFields(
                                fieldWithPath("description").type(STRING).attributes(getConstraint("최대 100자"))
                                        .description("그룹 설명").optional(),
                                fieldWithPath("headcount").type(NUMBER).attributes(getConstraint("1이상, 30이하"))
                                        .description("그룹 제한 인원 수").optional()
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("그룹 엔티티 아이디")
                        )));
    }

    @Test
    @DisplayName("그룹 목록 삭제")
    void deleteGroups() throws Exception {
        //given
        Member loginMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        GroupsDeleteRequest request = new GroupsDeleteRequest();
        request.setMemberId(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("groups-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("memberId").type(NUMBER).description("회원 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(STRING).description("짧은 메시지")
                        )));
    }

    @Test
    @DisplayName("그룹 목록 탈퇴")
    void quitGroups() throws Exception {
        //given
        Member loginMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        GroupsDeleteRequest request = new GroupsDeleteRequest();
        request.setMemberId(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/groups/quit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("groups-quit",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("memberId").type(NUMBER).description("회원 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(STRING).description("짧은 메시지")
                        )));
    }


    private GroupDto createTestGroupDto(Group group) {
        GroupDto groupDto = GroupDto.from(group);
        groupDto.setMemberSize(1);
        GroupStudyDto groupStudyDto = new GroupStudyDto(1L, 1L, "스터디", 0);
        groupDto.getStudies().add(groupStudyDto);
        groupDto.getGroupMembers().add(new GroupMemberDto(1L, 1L, 1L, "회원 이름", "회원 닉네임", LocalDateTime.now(), GroupRole.MASTER));
        return groupDto;
    }

}