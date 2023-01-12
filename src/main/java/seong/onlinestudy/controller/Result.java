package seong.onlinestudy.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import seong.onlinestudy.dto.GroupDto;

@Data
@NoArgsConstructor
public class Result<T> {

    T data;
    int number;
    int size;
    int totalPages;
    boolean hasNext;
    boolean hasPrevious;

    public void setPageInfo(Page page) {
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalPages = page.getTotalPages();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }

    public Result(T data) {
        this.data = data;
    }
}
