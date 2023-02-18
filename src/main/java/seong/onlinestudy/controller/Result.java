package seong.onlinestudy.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import seong.onlinestudy.dto.GroupDto;

@Data
@NoArgsConstructor
public class Result<T> {

    private String code;
    private T data;

    private int number;
    private int size;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public void setPageInfo(Page page) {
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalPages = page.getTotalPages();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }

    public Result(String code, T data) {
        this.code = code;
        this.data = data;
    }
}
