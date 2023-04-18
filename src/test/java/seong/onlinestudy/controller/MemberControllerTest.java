package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.docs.DocumentFormatGenerator;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.MemberDto;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.request.member.MemberDuplicateCheckRequest;
import seong.onlinestudy.request.member.MemberUpdateRequest;
import seong.onlinestudy.service.MemberService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getConstraint;

@AutoConfigureRestDocs
@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    MemberService memberService;

    MockHttpSession session;

    public MemberControllerTest() {
        session = new MockHttpSession();
    }

    @Test
    void createMember_Success() throws Exception {
        //given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("test123");
        request.setPassword("test123!");
        request.setPasswordCheck("test123!");
        request.setNickname("test12");

        given(memberService.createMember(any())).willReturn(1L);
        //when
        mvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        //then

    }

    @Test
    void createMember_비밀번호검증실패() throws Exception {
        //given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("test123");
        request.setPassword("test123"); //특수문자 미포함
        request.setNickname("test");

        //when
        mvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(rs -> {
                    assertThat(rs.getResolvedException())
                            .isInstanceOf(MethodArgumentNotValidException.class);
                })
                .andDo(print());

        //then
    }

    @Test
    void createMember_모두Null() throws Exception {
        //given
        MemberCreateRequest request = new MemberCreateRequest();

        //when
        mvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(rs -> {
                    assertThat(rs.getResolvedException())
                            .isInstanceOf(MethodArgumentNotValidException.class);
                })
                .andDo(print());

        //then
    }

    @Test
    public void createMember() throws Exception {
        //given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("member");
        request.setNickname("member");
        request.setPassword("member123!");
        request.setPasswordCheck("member123!");

        //when
        ResultActions resultActions = mvc.perform(post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("member-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").type(STRING)
                                        .attributes(getConstraint("영문, 숫자만 가능. 6자 이상, 20자 이하"))
                                        .description("회원 아이디"),
                                fieldWithPath("nickname").type(STRING)
                                        .attributes(getConstraint("2자 이상, 12자 이하"))
                                        .description("회원 닉네임").optional(),
                                fieldWithPath("password").type(STRING)
                                        .attributes(getConstraint("영문, 숫자, 특수문자 포함. 6자 이상, 20자 이하"))
                                        .description("회원 비밀번호"),
                                fieldWithPath("passwordCheck").type(STRING)
                                        .attributes(getConstraint("영문, 숫자, 특수문자 포함. 6자 이상, 20자 이하"))
                                        .description("회원 비밀번호 확인")
                                ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("생성된 회원 엔티티 아이디")
                        )));
    }

    @Test
    public void getMember() throws Exception {
        //given
        Member testMember = MyUtils.createMember("member", "member");
        ReflectionTestUtils.setField(testMember, "id", 1L);
        MemberDto memberDto = MemberDto.from(testMember);

        session.setAttribute(LOGIN_MEMBER, 1L);

        given(memberService.getMember(any())).willReturn(memberDto);

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/members/{memberId}", 1)
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("member-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("memberId").description("회원 엔티티 아이디")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("memberId").type(NUMBER).description("회원 엔티티 아이디"),
                                fieldWithPath("username").type(STRING).description("회원 아이디"),
                                fieldWithPath("nickname").type(STRING).description("회원 닉네임"),
                                fieldWithPath("deleted").type(BOOLEAN).description("회원 삭제여부")
                        )));
    }

    @Test
    public void duplicateCheck() throws Exception {
        //given
        MemberDuplicateCheckRequest request = new MemberDuplicateCheckRequest();
        request.setUsername("member");

        //when
        ResultActions resultActions = mvc.perform(post("/api/v1/members/duplicate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("member-duplicate-check",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").type(STRING).description("회원 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(BOOLEAN).description("중복되지 않으면 false")
                        )));
    }

    @Test
    public void updateMember() throws Exception {
        //given
        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setNickname("nickname");
        request.setPasswordOld("passwordOld123!");
        request.setPasswordNew("passwordNew1!");
        request.setPasswordNewCheck("passwordNew1!");

        Member testMember = MyUtils.createMember("member", "member");
        ReflectionTestUtils.setField(testMember, "id", 1L);
        session.setAttribute(LOGIN_MEMBER, 1L);

        given(memberService.updateMember(any(), any())).willReturn(1L);

        //when
        ResultActions resultActions = mvc.perform(patch("/api/v1/members/{memberId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("member-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("memberId").description("회원 엔티티 아이디")
                        ),
                        requestFields(
                                fieldWithPath("nickname").type(STRING)
                                        .attributes(getConstraint("2자 이상, 12자 이하"))
                                        .description("업데이틀 할 닉네임").optional(),
                                fieldWithPath("passwordOld").type(STRING)
                                        .attributes(getConstraint("영문, 숫자, 특수문자 포함. 6자 이상, 20자 이하"))
                                        .description("회원 현재 비밀번호").optional(),
                                fieldWithPath("passwordNew").type(STRING)
                                        .attributes(getConstraint("영문, 숫자, 특수문자 포함. 6자 이상, 20자 이하"))
                                        .description("업데이트 할 비밀번호").optional(),
                                fieldWithPath("passwordNewCheck").type(STRING)
                                        .attributes(getConstraint("영문, 숫자, 특수문자 포함. 6자 이상, 20자 이하"))
                                        .description("업데이트 할 비밀번호 확인").optional()
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("업데이트된 회원 엔티티 아이디")
                        )));
    }

    @Test
    public void deleteMember() throws Exception {
        //given
        Member testMember = MyUtils.createMember("member", "member");
        ReflectionTestUtils.setField(testMember, "id", 1L);
        session.setAttribute(LOGIN_MEMBER, 1L);

        //when
        ResultActions resultActions = mvc.perform(delete("/api/v1/members/{memberId}", 1)
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("member-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("memberId").description("회원 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("삭제된 회원 엔티티 아이디")
                        )));
    }
}