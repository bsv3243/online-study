package seong.onlinestudy.controller.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.service.TicketMessageService;
import seong.onlinestudy.service.TicketService;
import seong.onlinestudy.controller.websocket.TicketMessage;

import java.util.NoSuchElementException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/v1")
public class TicketMessageController {

    private final TicketRepository ticketRepository;
    private final SimpMessagingTemplate template;
    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;

    @MessageMapping("/groups/{id}")
    public void sendTicket(@DestinationVariable("id") Long groupId, TicketMessage ticketMessage) {
        Ticket ticket = ticketRepository.findById(ticketMessage.getTicketId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        template.convertAndSend("/sub/groups/"+groupId, TicketDto.from(ticket));
    }

    @MessageMapping("/groups")
    public void sendTicket(TicketMessage ticketMessage) {
        log.info("티켓메시지 ticketId={}", ticketMessage.getTicketId());

        MemberTicketDto memberTicket = ticketMessageService.getMemberTicket(ticketMessage);

        template.convertAndSend("/sub/groups/"+ticketMessage.getGroupId(), new Result<>("200", memberTicket));
    }
}
