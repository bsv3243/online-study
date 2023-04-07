package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import seong.onlinestudy.request.group.GroupsGetRequest;
import seong.onlinestudy.enumtype.OrderBy;
import seong.onlinestudy.service.GroupService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static seong.onlinestudy.MyUtils.createMember;
import static seong.onlinestudy.SessionConst.*;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getConstraint;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDefaultValue;
import static seong.onlinestudy.enumtype.GroupCategory.IT;

@AutoConfigureRestDocs
@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    GroupService groupService;

    MockHttpSession session;

    @BeforeEach
    void init() {
        session = new MockHttpSession();
    }

    @Test
    void 그룹생성_바인딩실패() throws Exception {
        //given
        GroupCreateRequest groupRequest = new GroupCreateRequest();
        groupRequest.setName("그룹");
        groupRequest.setHeadcount(50);

        Member member = createMember("mebmer", "member");

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
    @DisplayName("그룹 생성")
    void createGroup() throws Exception {
        //given
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("groupName"); request.setHeadcount(30); request.setCategory(IT);
        Member loginMember = createMember("memberA", "memberPassword");
        session.setAttribute(LOGIN_MEMBER, loginMember);

        given(groupService.createGroup(request, loginMember)).willReturn(1L);

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
        session.setAttribute(LOGIN_MEMBER, loginMember);

        given(groupService.joinGroup(1L, loginMember)).willReturn(1L);

        //when
        ResultActions result = mvc.perform(post("/api/v1/group/{groupId}/join", 1L)
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
        session.setAttribute(LOGIN_MEMBER, loginMember);

        //when
        ResultActions result = mvc.perform(post("/api/v1/group/{groupId}/quit", 1L)
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
        GroupsGetRequest request = new GroupsGetRequest();
        request.setPage(0); request.setSize(10); request.setCategory(IT);
        request.setSearch("검색어"); request.setStudyIds(List.of(1L,2L,3L));
        request.setOrderBy(OrderBy.CREATEDAT);

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
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("groups-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("page").type(NUMBER).attributes(getDefaultValue("0")).
                                        description("페이지"),
                                fieldWithPath("size").type(NUMBER).attributes(getDefaultValue("12"))
                                        .description("응답 데이터 개수"),
                                fieldWithPath("memberId").type(NUMBER).description("회원 엔티티 아이디").optional(),
                                fieldWithPath("category").type(STRING).description("그룹 카테고리(Enum Type)").optional(),
                                fieldWithPath("search").type(STRING).description("그룹 이름 검색어").optional(),
                                fieldWithPath("studyIds").type(JsonFieldType.ARRAY).description("스터디 아이디 목록").optional(),
                                fieldWithPath("orderBy").type(STRING).attributes(getDefaultValue("CREATEDAT"))
                                        .description("그룹 정렬 순서(Enum Type 탭 참고)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("그룹 목록"),
                                fieldWithPath("number").type(NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("size").type(NUMBER).description("페이지의 원소 개수"),
                                fieldWithPath("totalPages").type(NUMBER).description("총 페이지 수"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
                                fieldWithPath("hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 여부"),
                                fieldWithPath("data[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("data[].name").type(STRING).description("그룹 이름"),
                                fieldWithPath("data[].headcount").type(NUMBER).description("그룹 제한 인원 수"),
                                fieldWithPath("data[].memberSize").type(NUMBER).description("그룹 현제 인원 수"),
                                fieldWithPath("data[].createdAt").type(STRING).description("그룹 생성일"),
                                fieldWithPath("data[].description").type(STRING).description("그룹 설명"),
                                fieldWithPath("data[].category").type(STRING).description("그룹 카테고리"),
                                fieldWithPath("data[].groupMembers").type(JsonFieldType.ARRAY).description("그룹원 목록"),
                                fieldWithPath("data[].groupMembers[].groupMemberId").type(NUMBER).description("그룹원 엔티티 아이디"),
                                fieldWithPath("data[].groupMembers[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("data[].groupMembers[].memberId").type(NUMBER).description("회원 엔티티 아이디"),
                                fieldWithPath("data[].groupMembers[].username").type(STRING).description("회원 아이디"),
                                fieldWithPath("data[].groupMembers[].nickname").type(STRING).description("회원 닉네임"),
                                fieldWithPath("data[].groupMembers[].joinedAt").type(STRING).description("회원 그룹 가입일"),
                                fieldWithPath("data[].groupMembers[].role").type(STRING).description("회원 그룹 권한"),
                                fieldWithPath("data[].studies").type(JsonFieldType.ARRAY).description("그룹 스터디 목록"),
                                fieldWithPath("data[].studies[].studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                                fieldWithPath("data[].studies[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("data[].studies[].name").type(STRING).description("스터디 이름"),
                                fieldWithPath("data[].studies[].studyTime").type(NUMBER).description("총 스터디 시간")
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
        ResultActions result = mvc.perform(get("/api/v1/group/{groupId}", 1L));

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
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(OBJECT).description("그룹"),
                                fieldWithPath("data.groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("data.name").type(STRING).description("그룹 이름"),
                                fieldWithPath("data.headcount").type(NUMBER).description("그룹 제한 인원 수"),
                                fieldWithPath("data.memberSize").type(NUMBER).description("그룹 현제 인원 수"),
                                fieldWithPath("data.createdAt").type(STRING).description("그룹 생성일"),
                                fieldWithPath("data.description").type(STRING).description("그룹 설명"),
                                fieldWithPath("data.category").type(STRING).description("그룹 카테고리"),
                                fieldWithPath("data.groupMembers").type(JsonFieldType.ARRAY).description("그룹원 목록"),
                                fieldWithPath("data.groupMembers[].groupMemberId").type(NUMBER).description("그룹원 엔티티 아이디"),
                                fieldWithPath("data.groupMembers[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("data.groupMembers[].memberId").type(NUMBER).description("회원 엔티티 아이디"),
                                fieldWithPath("data.groupMembers[].username").type(STRING).description("회원 아이디"),
                                fieldWithPath("data.groupMembers[].nickname").type(STRING).description("회원 닉네임"),
                                fieldWithPath("data.groupMembers[].joinedAt").type(STRING).description("회원 그룹 가입일"),
                                fieldWithPath("data.groupMembers[].role").type(STRING).description("회원 그룹 권한"),
                                fieldWithPath("data.studies").type(JsonFieldType.ARRAY).description("그룹 스터디 목록"),
                                fieldWithPath("data.studies[].studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                                fieldWithPath("data.studies[].groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("data.studies[].name").type(STRING).description("스터디 이름"),
                                fieldWithPath("data.studies[].studyTime").type(NUMBER).description("총 스터디 시간")
                        )));
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup() throws Exception {
        //given
        Member loginMember = createMember("member", "member");

        session.setAttribute(LOGIN_MEMBER, loginMember);

        //when
        ResultActions result = mvc.perform(delete("/api/v1/group/{groupId}", 1L)
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

        session.setAttribute(LOGIN_MEMBER, loginMember);

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setDescription("그룹 설명"); request.setHeadcount(30);

        //when
        ResultActions result = mvc.perform(post("/api/v1/group/{groupId}", 1L)
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
        session.setAttribute(LOGIN_MEMBER, loginMember);

        GroupsDeleteRequest request = new GroupsDeleteRequest();
        request.setMemberId(1L);

        //when
        ResultActions result = mvc.perform(delete("/api/v1/groups")
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
        session.setAttribute(LOGIN_MEMBER, loginMember);

        GroupsDeleteRequest request = new GroupsDeleteRequest();
        request.setMemberId(1L);

        //when
        ResultActions result = mvc.perform(delete("/api/v1/groups/quit")
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