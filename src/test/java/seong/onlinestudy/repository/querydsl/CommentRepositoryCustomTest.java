package seong.onlinestudy.repository.querydsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.repository.CommentRepository;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.PostRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.MyUtils.*;

@DataJpaTest
class CommentRepositoryCustomTest {

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
    void findComments_회원조건() {
        //given
        Member testMember = createMember("member", "member");
        memberRepository.save(testMember);

        Post testPost = posts.get(0);
        List<Comment> testComments = createComments(List.of(testMember), List.of(testPost), 5, false);

        //when
        PageRequest pageRequest = PageRequest.of(0, 30);
        Page<Comment> findCommentsWithPage = commentRepository.findComments(testMember.getId(), null, pageRequest);

        //then
        List<Comment> findComments = findCommentsWithPage.getContent();

        assertThat(findComments.size()).isGreaterThan(0);
        assertThat(findComments).containsExactlyInAnyOrderElementsOf(testComments);
    }

    @Test
    void findComments_게시글조건() {
        //given
        Member testMember = createMember("member", "member");
        memberRepository.save(testMember);

        Post testPost = posts.get(0);
        List<Comment> testMemberComments = createComments(List.of(testMember), List.of(testPost), 5, false);

        //when
        PageRequest pageRequest = PageRequest.of(0, 30);
        Page<Comment> findCommentsWithPage = commentRepository.findComments(null, testPost.getId(), pageRequest);

        //then
        List<Comment> findComments = findCommentsWithPage.getContent();
        List<Comment> testComments = testPost.getComments();

        assertThat(findComments.size()).isGreaterThan(0);
        assertThat(findComments).containsExactlyInAnyOrderElementsOf(testComments);
        assertThat(findComments).containsAnyElementsOf(testMemberComments);
    }


}