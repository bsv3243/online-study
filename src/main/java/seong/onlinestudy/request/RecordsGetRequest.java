package seong.onlinestudy.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RecordsGetRequest {

    private Long studyId;
    private Long groupId;
    private Long memberId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    private int days;

    public boolean isAnySearchCondition() {
        return studyId != null || groupId != null;
    }

    public RecordsGetRequest() {
        startDate = LocalDate.now();
        days = 7;
    }
}
