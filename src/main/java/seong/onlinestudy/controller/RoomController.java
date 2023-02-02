package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.websocket.Message;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1")
public class RoomController {

    private final TicketRepository ticketRepository;
    private final SimpMessagingTemplate template;

    @MessageMapping("/room/{id}")
    public void sendTicket(@DestinationVariable("id") Long roomId, Message message) {
        Ticket ticket = ticketRepository.findById(message.getTicketId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        template.convertAndSend("/topic/room/"+roomId, TicketDto.from(ticket));
    }
}
