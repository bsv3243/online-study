package seong.onlinestudy.request;

import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;

import javax.validation.constraints.NotNull;

@Data
public class TicketUpdateRequest {

    @NotNull
    private TicketStatus ticketStatus;
}
