package seong.onlinestudy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.kafka.KafkaConst;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.service.TicketMessageService;
import seong.onlinestudy.service.TicketService;
import seong.onlinestudy.websocket.TicketMessage;

import java.util.NoSuchElementException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/v1")
public class TicketMessageController {

    private final KafkaTemplate<String, String> template;
    private final ObjectMapper mapper;

    @MessageMapping("/groups")
    public void sendTicket(TicketMessage ticketMessage) throws JsonProcessingException {

        template.send(KafkaConst.TICKET_MESSAGE_TOPIC, mapper.writeValueAsString(ticketMessage));
    }
}
