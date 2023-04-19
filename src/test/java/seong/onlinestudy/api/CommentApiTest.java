package seong.onlinestudy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.repository.CommentRepository;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.PostRepository;
import seong.onlinestudy.request.comment.CommentsGetRequest;

import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CommentApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;

    List<Member> members;
    List<Group> groups;
    List<Post> posts;
    List<Comment> comments;

    @BeforeEach
    void init() {
        members = createMembers(3);
        groups = createGroups(members, 2);

        joinMembersToGroups(members, groups);

        posts = createPosts(members, groups, 3, false);
        comments = createComments(members, posts, 10, false);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        postRepository.saveAll(posts);
        commentRepository.saveAll(comments);
    }

    @Test
    @DisplayName("댓글 목록 조회")
    void getComments() throws Exception {
        //given
        Member testMember = members.get(0);

        CommentsGetRequest request = new CommentsGetRequest();
        request.setMemberId(testMember.getId());

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }
}
