package seong.onlinestudy.repository.querydsl;

import seong.onlinestudy.domain.Ticket;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepositoryCustom {

    List<Ticket> findTickets(List<Long> memberIds, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime);
    List<Ticket> findTickets(Long memberId, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime);
}
