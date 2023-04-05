package seong.onlinestudy.request.post;

import lombok.Data;
import seong.onlinestudy.enumtype.PostCategory;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PostCreateRequest {

    @NotNull(message = "제목은 공란일 수 없습니다.")
    private String title;

    @NotNull(message = "본문은 공란일 수 없습니다.")
    private String content;

    @NotNull(message = "카테고리는 필수입니다.")
    private PostCategory category;

    private List<Long> studyIds;

    @NotNull(message = "그룹은 필수로 지정되어야 합니다.")
    private Long groupId;
}
