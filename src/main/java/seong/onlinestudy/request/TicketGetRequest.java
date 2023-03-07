package seong.onlinestudy.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TicketGetRequest {

    Long groupId;
    LocalDate date;
    int days;

    public TicketGetRequest() {
        date = LocalDate.now();
        days = 1;
    }
}
