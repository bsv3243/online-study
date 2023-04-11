package seong.onlinestudy.repository.jdbctemplate;

import seong.onlinestudy.domain.Ticket;

import java.util.List;

public interface JdbcTicketRecordRepository {

    /**
     * 티켓 기록을 일괄적으로 생성합니다.
     * @param tickets 만료되지 않은 티켓 목록
     */
    void insertTicketRecords(List<Ticket> tickets);
}
