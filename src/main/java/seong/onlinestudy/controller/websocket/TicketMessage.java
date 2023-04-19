package seong.onlinestudy.controller.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessage {

    private Long ticketId;
    private Long groupId;
}
