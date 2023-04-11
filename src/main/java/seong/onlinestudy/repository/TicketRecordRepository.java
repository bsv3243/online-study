package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.TicketRecord;
import seong.onlinestudy.repository.jdbctemplate.JdbcTicketRecordRepository;

public interface TicketRecordRepository extends JpaRepository<TicketRecord, Long>, JdbcTicketRecordRepository {

}
