package seong.onlinestudy.controller.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
public class Result<T> {
    private String code;
    private T data;

    public Result(String code, T data) {
        this.code = code;
        this.data = data;
    }
}
