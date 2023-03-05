package seong.onlinestudy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import seong.onlinestudy.repository.TicketRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final TicketRepository ticketRepository;

    @Scheduled(cron = "50 59 4 * * *")
    public void updateTicketStatus() {
        LocalDateTime endTime = LocalDateTime.now();
        ZoneOffset offset = ZoneOffset.of("+00:00");

        int updateCount = ticketRepository.updateTicketStatusToEnd(endTime, endTime.toEpochSecond(offset));
        log.info("{}개의 유효한 티켓의 상태가 종료 상태로 변경되었습니다.", updateCount);
    }
}
