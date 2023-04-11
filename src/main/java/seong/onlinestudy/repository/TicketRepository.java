package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.repository.querydsl.StudyTicketRepositoryCustom;
import seong.onlinestudy.repository.querydsl.TicketRepositoryCustom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, TicketRepositoryCustom, StudyTicketRepositoryCustom {

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
            " join fetch t.ticketRecord r" +
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
            " join fetch t.ticketRecord r" +
            " join m.groupMembers gm on gm.group.id = :groupId" +
            " where t.startTime >= :startTime and t.startTime < :endTime" +
            " order by t.member.id")
    List<Ticket> findTicketsByGroupId(@Param("groupId") Long groupId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 만료되지 않은 티켓들을 만료시킨다.
     * @return 업데이트 한 목록의 갯수를 반환
     */
    @Modifying
    @Query("update Ticket t set t.expired = true where t.expired = false")
    int expireTicketsWhereExpiredFalse();

    List<Ticket> findTicketsByExpiredFalse();
}
