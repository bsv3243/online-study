package seong.onlinestudy.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.MyUtils.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    EntityManager em;
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
        groups = createGroups(members,2);

        joinMembersToGroups(members, groups);

        posts = createPosts(members, groups, 3, false);
        comments = createComments(members, posts, 10, false);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        postRepository.saveAll(posts);
        commentRepository.saveAll(comments);
    }

    @Test
    void deleteComment() {
        //given
        Post testPost = posts.get(0);
        List<Comment> testComments = createComments(members, List.of(testPost), 5, false);

        testPost = postRepository.findById(testPost.getId()).get();
        assertThat(testPost.getComments()).containsAll(testComments);

        //when
        for (Comment testComment : testComments) {
            testComment.delete();
        }
        em.clear();

        //then
        Post newTestPost = postRepository.findById(testPost.getId()).get();
        assertThat(newTestPost.getComments()).doesNotContainAnyElementsOf(testComments);

    }

    @Test
    void deleteAllByMemberId() {
        //given
        Member testMember = members.get(0);
        Post testPost = posts.get(0);

        List<Comment> testComments = createComments(List.of(testMember), List.of(testPost), 20, false);
        commentRepository.saveAll(testComments);

        assertThat(testMember.getComments().size()).isGreaterThanOrEqualTo(20);
        em.flush();
        em.clear();

        //when
        commentRepository.deleteAllByMemberId(testMember.getId());
        em.flush();

        //then
        testMember = memberRepository.findById(testMember.getId()).get();
        assertThat(testMember.getComments().size()).isEqualTo(0);
    }

}