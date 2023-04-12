package seong.onlinestudy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import seong.onlinestudy.service.TicketService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final TicketService ticketService;

    @Scheduled(cron = "50 59 4 * * *")
    public void expireTickets() {
        ticketService.expireTicketsNotExpired();
    }
}
