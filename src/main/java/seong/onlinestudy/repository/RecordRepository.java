package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seong.onlinestudy.domain.Record;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    /**
     * 만료되지 않은 Ticket 들의 Record 에 대해서 expiredTime, activeTime 을 업데이트 한다.
     * @param expiredTime 만료 시간
     * @param expiredTimeToSeconds 만료 시간의 유닉스 타임
     * @param ticketIds 만료되지 않은 Ticket 들의 id
     */
    @Modifying
    @Query(value = "update Record r" +
            " set r.expired_time=:expiredTime," +
            " r.active_time=:expiredTimeToSeconds - datediff('second', '1970-01-01', (" +
            "select t.start_time from Ticket t where t.record_id=r.record_id))" +
            " where r.record_id in (select t.record_id from Ticket t where t.ticket_id in :ticketIds)", nativeQuery = true)
    void updateRecordsWhereExpiredFalse(@Param("expiredTime")LocalDateTime expiredTime,
                                        @Param("expiredTimeToSeconds") long expiredTimeToSeconds,
                                        @Param("ticketIds") List<Long> ticketIds);
}
