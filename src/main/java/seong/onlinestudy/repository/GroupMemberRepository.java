package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupMemberDto;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @Query("select gm from GroupMember gm" +
            " join fetch gm.group g" +
            " join fetch gm.member m" +
            " where gm.role='MASTER' and g in :groups")
    List<GroupMember> findGroupMasters(@Param("groups") List<Group> groups);

    @Query("select new seong.onlinestudy.dto.GroupMemberDto(" +
            "gm.id, g.id, m.id, m.username, m.nickname, gm.joinedAt, gm.role)" +
            " from GroupMember gm" +
            " join gm.group g" +
            " join gm.member m" +
            " where gm.role='MASTER' and g.id in :groupIds")
    List<GroupMemberDto> findGroupMastersInGroupIds(@Param("groupIds") List<Long> groupIds);

    @Modifying
    @Query("delete from GroupMember gm where gm.member.id = :memberId and gm.role <> 'MASTER'")
    void deleteAllByMemberIdRoleIsNotMaster(@Param("memberId") Long memberId);

    void deleteByGroupAndMember(Group group, Member member);
}
