package seong.onlinestudy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.repository.*;
import seong.onlinestudy.request.post.PostsGetRequest;

import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class PostApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;

    MockHttpSession session;

    List<Member> members;
    List<Group> groups;
    List<Post> posts;
    List<Comment> comments;

    public PostApiTest() {
        session = new MockHttpSession();
    }

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
    void getPosts_검색어조건() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("search", "검색");

        Member testMember = members.get(0);
        Post testPost1 = createPost("검색", "검색테스트", PostCategory.CHAT, testMember);
        Post testPost2 = createPost("검색검검색", "검색테스트", PostCategory.CHAT, testMember);
        Post testPost3 = createPost("테스트", "검색테스트", PostCategory.CHAT, testMember);

        postRepository.saveAll(List.of(testPost1, testPost2, testPost3));

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/posts")
                .params(request));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }
}
