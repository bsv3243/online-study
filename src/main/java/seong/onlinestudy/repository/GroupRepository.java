package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
