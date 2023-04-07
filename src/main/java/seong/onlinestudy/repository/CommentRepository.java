package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.repository.querydsl.CommentRepositoryCustom;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    @Modifying
    void deleteAllByMemberId(Long memberId);
}
