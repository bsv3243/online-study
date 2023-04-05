package seong.onlinestudy.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.enumtype.PostCategory;
import seong.onlinestudy.repository.CommentRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.PostRepository;
import seong.onlinestudy.request.comment.CommentCreateRequest;
import seong.onlinestudy.request.comment.CommentUpdateRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.createMember;
import static seong.onlinestudy.MyUtils.createPost;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    CommentService commentService;
    @Mock
    PostRepository postRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    MemberRepository memberRepository;

    @Test
    @DisplayName("댓글 생성")
    void createComment() {
        //given
        String content = "commentA";

        Member member = createMember("memberA", "memberA");
        setField(member, "id", 1L);
        Post post = createPost("test", "test", PostCategory.CHAT, member);
        setField(post, "id", 1L);
        Comment comment = MyUtils.createComment(content);
        setField(comment, "id", 1L);

        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent(content);

        given(memberRepository.findById(any())).willReturn(Optional.of(member));
        given(postRepository.findById(any())).willReturn(Optional.of(post));

        //when
        Long commentId = commentService.createComment(request, member);

        //then
        assertThat(post.getComments().size()).isEqualTo(1);
        assertThat(post.getComments().get(0).getContent()).isEqualTo(request.getContent());
    }

    @Test
    @DisplayName("댓글 업데이트")
    void updateComment() {
        String content = "content";

        Member member = createMember("memberA", "memberA");
        setField(member, "id", 1L);

        Post post = createPost("postA", "postA", PostCategory.CHAT, member);
        setField(post, "id", 1L);

        Comment comment = MyUtils.createComment(content);
        comment.setMemberAndPost(member, post);
        setField(comment, "id", 1L);

        String newContent = "newContent";
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent(newContent);

        given(commentRepository.findById(any())).willReturn(Optional.of(comment));

        //when
        Long updateCommentId = commentService.updateComment(comment.getId(), request, member);

        //then
        assertThat(comment.getContent()).isEqualTo(request.getContent());
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() {
        //given
        String content = "content";

        Member member = createMember("memberA", "memberA");
        setField(member, "id", 1L);

        Post post = createPost("postA", "postA", PostCategory.CHAT, member);
        setField(post, "id", 1L);

        List<Comment> comments = MyUtils.createComments(List.of(member), List.of(post), 10, true);

        Comment comment = MyUtils.createComment(content);
        comment.setMemberAndPost(member, post);
        setField(comment, "id", 10L);

        given(commentRepository.findById(any())).willReturn(Optional.of(comment));
        assertThat(post.getComments()).contains(comment);

        //when
        Long commentId = commentService.deleteComment(comment.getId(), member);

        //then
        assertThat(comment.getDeleted()).isTrue();

    }
}