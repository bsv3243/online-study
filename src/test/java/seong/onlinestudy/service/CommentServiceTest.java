package seong.onlinestudy.service;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.domain.PostCategory;
import seong.onlinestudy.repository.PostRepository;
import seong.onlinestudy.request.CommentCreateRequest;

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

    @Test
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

        given(postRepository.findById(any())).willReturn(Optional.of(post));

        //when
        Long commentId = commentService.createComment(request, member);

        //then
        assertThat(post.getComments().size()).isEqualTo(1);
        assertThat(post.getComments().get(0).getContent()).isEqualTo(request.getContent());
    }
}