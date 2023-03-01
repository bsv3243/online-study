package seong.onlinestudy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import seong.onlinestudy.domain.Member;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.domain.Study;

import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {

    Optional<Study> findByName(String name);
    Page<Study> findAllByNameContains(String name, Pageable pageable);

}
