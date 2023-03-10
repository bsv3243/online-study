package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.websocket.TicketMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketMessageService {

    private final TicketRepository ticketRepository;

    public MemberTicketDto getMemberTicket(TicketMessage ticketMessage) {
        Ticket ticket = ticketRepository.findById(ticketMessage.getTicketId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        Member member = ticket.getMember();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDate.now().atStartOfDay().plusHours(5);
        if(now.getHour() < 4) {
            startTime = startTime.minusDays(1);
        }

        List<Ticket> tickets = ticketRepository.findTickets(member, startTime, startTime.plusDays(1));

        return MemberTicketDto.from(member, tickets);
    }
}
