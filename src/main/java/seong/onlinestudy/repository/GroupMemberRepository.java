package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupMemberCountDto;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @Query("select gm from GroupMember gm" +
            " join fetch gm.group g" +
            " join fetch gm.member m" +
            " where gm.role='MASTER' and g in :groups")
    List<GroupMember> findGroupMasters(@Param("groups") List<Group> groups);

    void deleteByMemberId(Long memberId);

    void deleteByGroupAndMember(Group group, Member member);
}
