package seong.onlinestudy.request;

import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;

import javax.validation.constraints.NotNull;

@Data
public class TicketCreateRequest {

    @NotNull(message = "스터디 지정은 필수입니다.")
    private Long studyId;

    @NotNull(message = "그룹 지정은 필수입니다.")
    private Long groupId;

    @NotNull(message = "상태 지정은 필수입니다.")
    private TicketStatus status;
}
