package seong.onlinestudy.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TicketGetRequest {

    Long groupId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;
    int days;

    public TicketGetRequest() {
        date = LocalDate.now();
        days = 1;
    }
}
