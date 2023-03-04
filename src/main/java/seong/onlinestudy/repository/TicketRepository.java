package seong.onlinestudy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.domain.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByMemberAndTicketStatusIn(Member member, List<TicketStatus> statuses);

    /**
     * Member 로부터 groupId가 일치하는 Group 을 조인하고 생성 시점이 startTime 이상,
     * endTime 미만인 Ticket 을 left 조인한 목록을 조회한다.
     * @param startTime 조회 시작 시점
     * @param endTime 조회 종료 시점
     * @param groupId Group 의 id
     * @return Ticket 을 페치 조인한 Member 리스트를 반환
     */
    @Query("select m from Member m" +
            " join m.groupMembers gm join gm.group g on g.id = :groupId" +
            " left join m.tickets t on t.startTime >= :startTime and t.endTime < :endTime" +
            " order by gm.joinedAt asc, t.startTime asc")
    List<Member> findMembersWithTickets(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("groupId") Long groupId);

    @Query("select m from Member m join m.groupMembers gm join gm.group g on g.id=:groupId")
    List<Member> findMembersWithTickets(@Param("groupId") Long groupId);
}
