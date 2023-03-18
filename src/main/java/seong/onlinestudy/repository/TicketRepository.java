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

    Optional<Ticket> findByMemberAndExpiredFalse(Member member);

    /**
     * Ticket 의 생성 시간이 startTime 이상이고, endTime 미만이며 member 가 가진 티켓 목록을 반환한다.
     * @param member Member
     * @param startTime 시작시간
     * @param endTime 종료시간
     * @return Study 와 페치 조인한 ticket 리스트를 반환
     */
    @Query("select t from Ticket t" +
            " join t.member m on m = :member" +
            " join fetch t.study s" +
            " where t.startTime >= :startTime and t.startTime < :endTime")
    List<Ticket> findTickets(@Param("member") Member member,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * Ticket 의 생성 시간이 startTime 이상, endTime 미만이며 그룹에 속한 member 의 ticket 리스트를 반환한다.
     * @param groupId Group 의 id
     * @param startTime 시작시간
     * @param endTime 종료시간
     * @return Member, Study 와 페치 조인한 티켓 리스트를 반환
     */
    @Query("select t from Ticket t" +
            " join fetch t.member m" +
            " join fetch t.study s" +
            " join m.groupMembers gm on gm.group.id = :groupId" +
            " where t.startTime >= :startTime and t.startTime < :endTime" +
            " order by t.member.id")
    List<Ticket> findTickets(@Param("groupId") Long groupId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

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
            " set t.is_expired = true," +
            " t.end_time=:endTime," +
            " t.active_time=:endTimeToSecond-datediff('second', '1970-01-01 09:00:00', t.start_time)" +
            " where t.is_expired = false", nativeQuery = true)
    int updateTicketStatusToEnd(@Param("endTime") LocalDateTime endTime,
                                @Param("endTimeToSecond") long endTimeToSecond);
}
