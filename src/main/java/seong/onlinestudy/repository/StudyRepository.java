package seong.onlinestudy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Study;

import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {

    Optional<Study> findByName(String name);
    Page<Study> findAllByNameContains(String name, Pageable pageable);
}
