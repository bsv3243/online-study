package seong.onlinestudy.docs;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import seong.onlinestudy.controller.response.PageResult;
import seong.onlinestudy.controller.response.Result;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    @GetMapping("/page-result")
    public Result<String> getPageResult() {
        return new PageResult<>("200", "데이터", new PageImpl<>(List.of("데이터")));
    }

    @GetMapping("/result")
    public Result<String> geResult() {
        return new Result<>("200", "데이터");
    }


}
