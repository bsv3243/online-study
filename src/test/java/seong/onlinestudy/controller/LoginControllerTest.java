package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.request.login.LoginRequest;
import seong.onlinestudy.service.LoginService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.constant.SessionConst.LOGIN_MEMBER;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getConstraint;

@AutoConfigureRestDocs
@WebMvcTest(controllers = LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(RestDocumentationExtension.class)
class LoginControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    LoginService loginService;

    MockHttpSession session;

    public LoginControllerTest() {
        session = new MockHttpSession();
    }

    @BeforeEach
    void init(WebApplicationContext context, RestDocumentationContextProvider provider) {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(provider))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    public void login() throws Exception {
        //given
        LoginRequest request = new LoginRequest();
        request.setUsername("member"); request.setPassword("member123!");

        Member loginMember = MyUtils.createMember("member", "member");
        ReflectionTestUtils.setField(loginMember, "id", 1L);

        given(loginService.login(any())).willReturn(loginMember);

        //when
        ResultActions resultActions = mvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").type(STRING)
                                        .attributes(getConstraint("영문, 숫자만 가능. 6자 이상, 20자 이하"))
                                        .description("회원 아이디"),
                                fieldWithPath("password").type(STRING)
                                        .attributes(getConstraint("영문, 숫자, 특수문자를 포함. 6자 이상, 20자 이하"))
                                        .description("회원 비밀번호")
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
    public void logout() throws Exception {
        //given
        Member loginMember = MyUtils.createMember("member", "member");
        ReflectionTestUtils.setField(loginMember, "id", 1L);

        session.setAttribute(LOGIN_MEMBER, 1L);

        //when
        ResultActions resultActions = mvc.perform(post("/api/v1/logout")
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("logout",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(STRING).description("logout 성공 메시지")
                        )));
    }

}