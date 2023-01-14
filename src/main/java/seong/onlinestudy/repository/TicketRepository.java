package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
