package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.domain.PostStudy;

import java.util.List;

public interface PostStudyRepository extends JpaRepository<PostStudy, Long> {

    /**
     * PostStudy 로부터 Study 엔티티를 페치 조인하여 Post 엔티티와 연관관계를 맺은 목록을 조회한다.
     * @param post Post 엔티티
     * @return Study 와 페치 조인한 PostStudy 리스트를 반환
     */
    @Query("select ps from PostStudy ps join fetch ps.study s" +
            " where ps.post=:post")
    List<PostStudy> findStudiesWherePost(@Param("post") Post post);

    /**
     * PostStudy 로부터 Study, Post 엔티티를 페치 조인하여 Post 엔티티와 연관관계를 맺은 목록을 조회한다.
     * @param posts Post 리스트
     * @return Study, Post 와 페치 조인한 PostStudy 리스트를 반환
     */
    @Query("select ps from PostStudy ps join fetch ps.study s join fetch ps.post p" +
            " where p in :posts")
    List<PostStudy> findStudiesWhereInPosts(@Param("posts") List<Post> posts);
}
