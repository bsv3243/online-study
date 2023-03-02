package seong.onlinestudy.request;

import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;

import javax.validation.constraints.NotNull;

@Data
public class TicketCreateRequest {

    @NotNull
    private Long studyId;

    @NotNull
    private Long groupId;

    @NotNull
    private TicketStatus status;
}
