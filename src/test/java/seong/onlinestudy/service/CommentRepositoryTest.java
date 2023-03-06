package seong.onlinestudy.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.domain.PostCategory;
import seong.onlinestudy.repository.CommentRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.PostRepository;

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
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;

    @Test
    void deleteComment() {
        Member member = createMember("member", "member");
        memberRepository.save(member);

        Post post = createPost("title", "content", PostCategory.CHAT, member);
        postRepository.save(post);

        Comment comment = createComment("content");
        comment.setMemberAndPost(member, post);

        List<Comment> comments = createComments(List.of(member), List.of(post), false, 10);
        em.flush();

        //when
        Comment findComment = commentRepository.findById(comment.getId()).get();
        findComment.delete();
        em.flush();

        //then
        Post findPost = postRepository.findById(post.getId()).get();
        assertThat(findPost.getComments()).containsExactlyInAnyOrderElementsOf(comments);
        assertThat(findPost.getComments()).doesNotContain(findComment);
        assertThat(findComment.getPost()).isNull();

    }

}