package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.dto.GroupMemberDto;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryCustom {

}
