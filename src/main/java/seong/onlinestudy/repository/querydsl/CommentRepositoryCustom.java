package seong.onlinestudy.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Comment;

public interface CommentRepositoryCustom {

    Page<Comment> findComments(Long memberId, Long postId, Pageable pageable);
}
