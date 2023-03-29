package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.PostCategory;
import seong.onlinestudy.dto.CommentDto;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.dto.MemberDto;
import seong.onlinestudy.dto.PostDto;
import seong.onlinestudy.request.post.PostsGetRequest;
import seong.onlinestudy.service.PostService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureRestDocs
class PostControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    MockHttpSession session;

    @MockBean
    PostService postService;

    public PostControllerTest() {
        session = new MockHttpSession();
    }

    @Test
    public void getPosts() throws Exception {
        //given
        PostsGetRequest request = new PostsGetRequest();
        request.setGroupId(1L); request.setSearch("검색어");
        request.setCategory(PostCategory.CHAT); request.setStudyIds(List.of(1L));

        Member member = MyUtils.createMember("member", "member");
        MemberDto memberDto = MemberDto.from(member);

        GroupDto groupDto = createGroupDto(member);
        CommentDto commentDto = createCommentDto(memberDto);
        PostDto postDto = createPostDto(memberDto, groupDto, commentDto);

        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        given(postService.getPosts(any())).willReturn(new PageImpl<>(List.of(postDto), pageRequest, 1L));

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("posts-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("page").type(JsonFieldType.NUMBER).description("페이지(기본값: 0)"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER).description("한 페이지의 사이즈(기본값: 10)"),
                                fieldWithPath("groupId").type(JsonFieldType.NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("search").type(JsonFieldType.STRING).description("게시글 제목 대상 검색어"),
                                fieldWithPath("category").type(JsonFieldType.STRING).description("게시글 카테고리"),
                                fieldWithPath("studyIds").type(JsonFieldType.ARRAY).description("스터디 엔티티 아이디 목록"),
                                fieldWithPath("deleted").type(JsonFieldType.BOOLEAN).description("게시글 삭제 여부(기본값: false)")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("postId").type(NUMBER).description("게시글 엔티티 아이디"),
                                fieldWithPath("title").type(STRING).description("게시글 엔티티 아이디"),
                                fieldWithPath("content").type(STRING).description("게시글 엔티티 아이디"),
                                fieldWithPath("category").type(STRING).description("게시글 엔티티 아이디"),
                                fieldWithPath("createdAt").type(STRING).description("게시글 엔티티 아이디"),
                                fieldWithPath("viewCount").type(NUMBER).description("게시글 엔티티 아이디"),
                                subsectionWithPath("member").type(OBJECT).description("게시글 작성자"),
                                subsectionWithPath("group").type(OBJECT).description("게시글 연관된 그룹"),
                                subsectionWithPath("postStudies").type(ARRAY).description("게시글 연관된 스터디 목록"),
                                subsectionWithPath("comments").type(ARRAY).description("게시글의 댓글 목록")
                        )));


    }

    private GroupDto createGroupDto(Member member) {
        Group group = MyUtils.createGroup("그룹", 30, member);
        GroupDto groupDto = GroupDto.from(group);
        return groupDto;
    }

    private PostDto createPostDto(MemberDto memberDto, GroupDto groupDto, CommentDto commentDto) {
        PostDto postDto = new PostDto();
        postDto.setPostId(1L);
        postDto.setTitle("제목");
        postDto.setContent("내용");
        postDto.setCategory(PostCategory.CHAT);
        postDto.setCreatedAt(LocalDateTime.now());
        postDto.setViewCount(1);
        postDto.setMember(memberDto);
        postDto.setGroup(groupDto);
        postDto.setComments(List.of(commentDto));
        return postDto;
    }

    private CommentDto createCommentDto(MemberDto memberDto) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentId(1L);
        commentDto.setContent("내용");
        commentDto.setPostId(1L);
        commentDto.setMember(memberDto);
        commentDto.setCreatedAt(LocalDateTime.now());
        return commentDto;
    }
}