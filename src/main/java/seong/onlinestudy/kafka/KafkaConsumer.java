package seong.onlinestudy.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.service.TicketMessageService;
import seong.onlinestudy.websocket.TicketMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final SimpMessagingTemplate template;
    private final TicketMessageService ticketMessageService;
    private final ObjectMapper mapper;

    @KafkaListener(topics = KafkaConst.TICKET_MESSAGE_TOPIC, groupId = "group1")
    public void ticketMessageConsumer(String message) throws JsonProcessingException {
        TicketMessage ticketMessage = mapper.readValue(message, TicketMessage.class);

        log.info("티켓메시지 {}", ticketMessage);
        MemberTicketDto memberTicket = ticketMessageService.getMemberTicket(ticketMessage);
        template.convertAndSend("/sub/group/"+ticketMessage.getGroupId(), memberTicket);
    }
}
