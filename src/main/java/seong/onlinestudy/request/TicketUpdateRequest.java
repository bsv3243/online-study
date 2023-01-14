package seong.onlinestudy.request;

import lombok.Data;
import seong.onlinestudy.domain.MemberStatus;

import javax.validation.constraints.NotNull;

@Data
public class TicketUpdateRequest {

    @NotNull
    private MemberStatus memberStatus;
}
