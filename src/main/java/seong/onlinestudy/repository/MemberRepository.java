package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupMemberDto;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

//    @Query("select" +
//            " new seong.onlinestudy.dto.GroupMemberDto(gm.id, g.id, m.id, m.username, m.nickname, gm.joinedAt, gm.role)" +
//            " from Member m join m.groupMembers gm join gm.group g where g in :content and gm.role='MASTER'")
//    List<GroupMemberDto> findGroupMasters(@Param("content") List<Group> content);
}
