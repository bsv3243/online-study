package seong.onlinestudy.controlleradvice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResult {

    private String code;
    private List<String> message = new ArrayList<>();

    public ErrorResult(String code, String message) {
        this.code = code;
        this.message.add(message);
    }
}
