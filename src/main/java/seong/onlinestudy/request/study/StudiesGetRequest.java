package seong.onlinestudy.request.study;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class StudiesGetRequest {

    private String name;
    private Long memberId;
    private Long groupId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    private Integer days;
    private int page;
    private int size;

    public StudiesGetRequest() {
        page = 0;
        size = 10;
        name = "";
    }
}
