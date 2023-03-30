package seong.onlinestudy.request.study;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class StudiesGetRequest {

    private int page;
    private int size;
    private String name;
    private Long memberId;
    private Long groupId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    private int days;

    public StudiesGetRequest() {
        page = 0;
        size = 10;
        name = "";
        date = LocalDate.now().minusDays(6);
        days = 7;
    }
}