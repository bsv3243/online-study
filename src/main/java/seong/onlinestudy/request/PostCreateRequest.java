package seong.onlinestudy.request;

import lombok.Data;
import seong.onlinestudy.domain.PostCategory;

import java.util.List;

@Data
public class PostCreateRequest {

    private String title;
    private String content;
    private PostCategory category;
    private List<Long> studyIds;
    private Long groupId;
}
