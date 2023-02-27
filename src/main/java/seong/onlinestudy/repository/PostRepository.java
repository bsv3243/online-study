package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Post;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Post 로부터 Member, Group 엔티티를 페치 조인하고 postId와 일치하는 게시글을 조회한다.
     * @param postId Post 엔티티의 Id
     * @return Member, Group 과 페치 조인한 Post 엔티티 1개 반환
     */
    @Query("select p from Post p left join fetch p.member m left join fetch p.group g where p.id=:postId")
    Optional<Post> findByIdWithMemberAndGroup(@Param("postId") Long postId);
}
