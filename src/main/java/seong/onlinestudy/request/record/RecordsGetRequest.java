package seong.onlinestudy.request.record;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import seong.onlinestudy.TimeConst;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecordsGetRequest {

    private Long studyId;
    private Long groupId;
    private Long memberId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    private int days;

    public RecordsGetRequest() {
        days = 7;

        LocalDateTime now = LocalDateTime.now();
        if(now.getHour() < TimeConst.DAY_START) {
            startDate = now.toLocalDate();
        } else {
            startDate = now.toLocalDate().plusDays(1);
        }
        startDate = startDate.minusDays(days);
    }
}
