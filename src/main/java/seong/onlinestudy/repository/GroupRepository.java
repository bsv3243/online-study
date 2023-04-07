package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.repository.querydsl.GroupRepositoryCustom;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryCustom {

    /**
     * Group 의 Id와 groupId가 일치하는 목록을 조회한다.
     * @param groupId
     * @return GroupMember, Member 를 페치 조인한 리스트를 반환
     */
    @Query("select g from Group g join fetch g.groupMembers gm join fetch gm.member where g.id=:groupId")
    Optional<Group> findGroupWithMembers(@Param("groupId") Long groupId);

    @Modifying
    @Query("update Group g set g.deleted = true" +
            " where g in (select gm.group from GroupMember gm" +
            " where gm.member.id=:memberId and gm.role='MASTER')")
    void softDeleteAllByMemberIdRoleIsMaster(@Param("memberId") Long memberId);
}
