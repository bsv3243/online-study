package seong.onlinestudy.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TicketCreateRequest {

    @NotNull
    private Long studyId;

    @NotNull
    private Long roomId;
}
