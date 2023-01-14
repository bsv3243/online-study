package seong.onlinestudy.request;

import lombok.Data;

@Data
public class StudySearchCond {

    private int page;
    private int size;
    private String name;

    public StudySearchCond() {
        page = 0;
        size = 10;
    }
}
