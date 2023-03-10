package seong.onlinestudy.request;

import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;

import javax.validation.constraints.NotNull;

@Data
public class TicketUpdateRequest {

    @NotNull(message = "티켓 업데이트 상태가 지정되지 않았습니다.")
    private TicketStatus status;
}
