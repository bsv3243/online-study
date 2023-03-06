package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
