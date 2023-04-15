package seong.onlinestudy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.docs.DocumentFormatGenerator;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.CommentDto;
import seong.onlinestudy.dto.MemberDto;
import seong.onlinestudy.request.CommentsGetRequest;
import seong.onlinestudy.request.comment.CommentCreateRequest;
import seong.onlinestudy.request.comment.CommentUpdateRequest;
import seong.onlinestudy.request.comment.CommentsDeleteRequest;
import seong.onlinestudy.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.createMember;
import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDefaultValue;


@AutoConfigureRestDocs
@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    CommentService commentService;

    MockHttpSession session;

    public CommentControllerTest() {
        session = new MockHttpSession();
    }

    @Test
    void createComment() throws Exception {
        //given
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("댓글 내용");
        request.setPostId(1L);

        Member testMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        given(commentService.createComment(any(), any())).willReturn(1L);

        //when
        ResultActions resultActions = mvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        resultActions.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("comment-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글 내용"),
                                fieldWithPath("postId").type(NUMBER).description("게시글 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("생성된 댓글 엔티티 아이디")
                        )));
    }

    @Test
    void getComments() throws Exception {
        //given
        CommentsGetRequest request = new CommentsGetRequest();
        request.setMemberId(1L); request.setPage(0);
        request.setSize(10); request.setPostId(1L);

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("memberId", "1");
        requestMap.add("postId", "1");
        requestMap.add("page", "0");
        requestMap.add("size", "10");

        MemberDto memberDto = new MemberDto();
        memberDto.setMemberId(1L); memberDto.setNickname("member");
        memberDto.setUsername("member");

        CommentDto commentDto = new CommentDto();
        commentDto.setCommentId(1L); commentDto.setContent("댓글 내용");
        commentDto.setMember(memberDto); commentDto.setPostId(1L);
        commentDto.setCreatedAt(LocalDateTime.now());

        PageRequest pageRequest = PageRequest.of(0, 10);
        PageImpl<CommentDto> commentDtosWithPage
                = new PageImpl<>(List.of(commentDto), pageRequest, 1);

        given(commentService.getComments(request)).willReturn(commentDtosWithPage);

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/comments")
                        .params(requestMap));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("comments-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("memberId").description("회원 엔티티 아이디").optional(),
                                parameterWithName("postId").description("게시글 엔티티 아이디").optional(),
                                parameterWithName("page").attributes(getDefaultValue("0")).description("페이지 번호"),
                                parameterWithName("size").attributes(getDefaultValue("30")).description("페이지 사이즈")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("commentId").type(NUMBER).description("댓글 엔티티 아이디"),
                                fieldWithPath("content").type(STRING).description("댓글 내용"),
                                fieldWithPath("createdAt").type(STRING).description("댓글 작성일"),
                                fieldWithPath("postId").type(NUMBER).description("연관된 게시글 엔티티 아이디"),
                                fieldWithPath("deleted").type(BOOLEAN).description("댓글 삭제 여부"),

                                subsectionWithPath("member").type(OBJECT).description("댓글 작성자"),
                                fieldWithPath("member.memberId").type(NUMBER).description("댓글 작성자 엔티티 아이디"),
                                fieldWithPath("member.username").type(STRING).description("댓글 작성자 아이디"),
                                fieldWithPath("member.nickname").type(STRING).description("댓글 작성자 닉네임")
                        )));
    }

    @Test
    void updateComment() throws Exception {
        //given
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent("댓글 내용");

        Member testMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        given(commentService.updateComment(any(), any(), any())).willReturn(1L);

        //when
        ResultActions resultActions = mvc.perform(patch("/api/v1/comment/{commentId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("comment-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("commentId").description("댓글 엔티티 아이디")
                        ),
                        requestFields(
                                fieldWithPath("content").type(STRING).description("댓글 내용")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("업데이트 된 댓글 엔티티 아이디")
                        )));
    }

    @Test
    void deleteComment() throws Exception {
        //given
        Member testMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        given(commentService.deleteComment(any(), any())).willReturn(1L);

        //when
        ResultActions resultActions = mvc.perform(delete("/api/v1/comment/{commentId}", 1L)
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("comment-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("commentId").description("댓글 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("삭제된 댓글 엔티티 아이디")
                        )));
    }

    @Test
    void deleteComments() throws Exception {
        //given
        CommentsDeleteRequest request = new CommentsDeleteRequest();
        request.setMemberId(1L);

        Member testMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        //when
        ResultActions resultActions = mvc.perform(post("/api/v1/comments/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("comments-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("memberId").type(NUMBER).description("회원 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(STRING).description("삭제 미시지")
                        )));
    }
}