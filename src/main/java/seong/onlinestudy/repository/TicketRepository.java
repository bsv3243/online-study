package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 업데이트 대상은 Ticket 의 ticketStatus 가 END 가 아닌 Ticket 들로 한다.
     * Ticket 의 ticketStatus 를 END 로, endTime 을 주어진 endTime 으로,
     * activeTime 을 endTime 과 startTime 의 유닉스 타임을 뺀 값으로 업데이트 한다.
     * 본 메서드는 H2 데이터베이스에 의존한다.
     * @param endTime Ticket 만료 시간
     * @param endTimeToSecond Ticket 만료 시간의 유닉스 타임(+09:00)
     * @return 업데이트 한 목록의 갯수를 반환
     */
    @Modifying
    @Query(value = "update Ticket t" +
            " set t.ticket_status='END'," +
            " t.end_time=:endTime," +
            " t.active_time=:endTimeToSecond-datediff('second', '1970-01-01 09:00:00', t.start_time)" +
            " where t.ticket_status != 'END'", nativeQuery = true)
    int updateTicketStatusToEnd(@Param("endTime") LocalDateTime endTime,
                                @Param("endTimeToSecond") long endTimeToSecond);
}
