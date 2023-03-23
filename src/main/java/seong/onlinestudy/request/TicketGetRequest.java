package seong.onlinestudy.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TicketGetRequest {

    Long groupId;
    Long studyId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;
    int days;

    public TicketGetRequest() {
        date = LocalDate.now();
        days = 1;
    }

    public boolean isAnySearchCondition() {
        return groupId != null || studyId != null;
    }
}
