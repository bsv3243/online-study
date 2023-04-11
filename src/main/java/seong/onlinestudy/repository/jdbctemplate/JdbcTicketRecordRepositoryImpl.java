package seong.onlinestudy.repository.jdbctemplate;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import seong.onlinestudy.domain.Ticket;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RequiredArgsConstructor
public class JdbcTicketRecordRepositoryImpl implements JdbcTicketRecordRepository {

    private final JdbcTemplate template;

    @Override
    public void insertTicketRecords(List<Ticket> tickets) {
        String sql = "insert into Ticket_Record (expired_time, active_time, ticket_id) values (?, ?, ?)";
        template.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ZoneOffset offset = ZoneOffset.of("+09:00");

                Ticket ticket = tickets.get(i);

                LocalDateTime expiredTime = LocalDateTime.now();
                long activeTime = expiredTime.toEpochSecond(offset) - ticket.getStartTime().toEpochSecond(offset);

                ps.setTimestamp(1, Timestamp.valueOf(expiredTime));
                ps.setLong(2, activeTime);
                ps.setLong(3, ticket.getId());
            }

            @Override
            public int getBatchSize() {
                return tickets.size();
            }
        });
    }
}
