package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.dto.GroupMemberDto;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryCustom {

    @Query("select g from Group g join fetch g.groupMembers gm join fetch gm.member where g.id=:groupId")
    Optional<Group> findGroupWithMembers(@Param("groupId") Long groupId);

}
