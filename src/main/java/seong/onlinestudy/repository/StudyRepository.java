package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.repository.querydsl.StudyRepositoryCustom;

import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {

    Optional<Study> findByName(String name);
}
