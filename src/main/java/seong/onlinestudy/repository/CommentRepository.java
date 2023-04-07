package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.repository.querydsl.CommentRepositoryCustom;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    @Modifying
    @Query("update Comment c set c.deleted = true" +
            " where c.member.id = :memberId")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);
}
