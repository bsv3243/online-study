package seong.onlinestudy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.repository.RecordRepository;
import seong.onlinestudy.repository.TicketRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final TicketRepository ticketRepository;
    private final RecordRepository recordRepository;

    @Scheduled(cron = "50 59 4 * * *")
    public void expiredTicketsAndUpdateRecords() {
        LocalDateTime endTime = LocalDateTime.now();
        ZoneOffset offset = ZoneOffset.of("+00:00");

        List<Ticket> ticketsNotExpired = ticketRepository.findTicketsByExpiredFalse();
        List<Long> studyIdsNotExpired = ticketsNotExpired.stream()
                .map(Ticket::getId).collect(Collectors.toList());

        recordRepository.updateRecordsWhereExpiredFalse(endTime,
                endTime.toEpochSecond(offset),
                studyIdsNotExpired);

        int updateCount = ticketRepository.expireTicketsWhereExpiredFalse();
        log.info("{}개의 유효한 티켓의 상태가 종료 상태로 변경되었습니다.", updateCount);
    }
}
