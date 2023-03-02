package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.domain.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByMemberAndTicketStatusIn(Member member, List<TicketStatus> statuses);
}
