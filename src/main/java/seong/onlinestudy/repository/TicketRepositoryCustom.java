package seong.onlinestudy.repository;

import seong.onlinestudy.domain.Ticket;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepositoryCustom {

    List<Ticket> findTickets(Long studyId, Long groupId, Long memberId, LocalDateTime startTime, LocalDateTime endTime);
}
