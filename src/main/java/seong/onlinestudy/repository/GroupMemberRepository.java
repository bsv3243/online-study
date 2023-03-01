package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.dto.GroupMemberCountDto;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @Query("select new seong.onlinestudy.dto.GroupMemberCountDto(g.id, count(gm.id)) from Group g join GroupMember gm" +
            " where g in :groups group by g.id")
    List<GroupMemberCountDto> countMemberInGroups(@Param("groups") List<Group> groups);
}
