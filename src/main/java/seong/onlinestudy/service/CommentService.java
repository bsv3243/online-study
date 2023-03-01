package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.repository.PostRepository;
import seong.onlinestudy.request.CommentCreateRequest;

import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Long createComment(CommentCreateRequest request, Member loginMember) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));

        Comment comment = Comment.create(request);
        comment.setMemberAndPost(loginMember, post);
        log.info("댓글이 작성되었습니다. postId={}, commentId={}, memberId={}",
                post.getId(), comment.getId(), loginMember.getId());

        return comment.getId();
    }
}
