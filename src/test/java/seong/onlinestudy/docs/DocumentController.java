package seong.onlinestudy.docs;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import seong.onlinestudy.controller.response.PageResult;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.enumtype.EnumType;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.enumtype.OrderBy;
import seong.onlinestudy.enumtype.PostCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/post-category")
    public Result<Map<String, String>> getPageCategory() {

        return new Result<>("200", getEnumTypes(PostCategory.values()));
    }

    @GetMapping("/group-category")
    public Result<Map<String, String>> getGroupCategory() {
        return new Result<>("200", getEnumTypes(GroupCategory.values()));
    }

    @GetMapping("/order-by")
    public Result<Map<String, String>> getOrderBy() {
        return new Result<>("200", getEnumTypes(OrderBy.values()));
    }

    private Map<String, String> getEnumTypes(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes).collect(Collectors.toMap(EnumType::getName, EnumType::getText));
    }
}
