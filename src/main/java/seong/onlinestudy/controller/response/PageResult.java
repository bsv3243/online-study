package seong.onlinestudy.controller.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<T>{
    private int number;
    private int size;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PageResult(String code, T data, Page page) {
        super(code, data);
        number = page.getNumber();
        size = page.getSize();
        totalPages = page.getTotalPages();
        hasNext = page.hasNext();
        hasPrevious = page.hasPrevious();
    }
}


