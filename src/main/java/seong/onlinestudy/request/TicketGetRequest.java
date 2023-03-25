package seong.onlinestudy.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TicketGetRequest {

    Long groupId;
    Long studyId;
    Long memberId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;
    int days;

    int page;
    int size;

    public TicketGetRequest() {
        date = LocalDate.now();
        days = 1;

        page = 0;
        size = 30;
    }
}
