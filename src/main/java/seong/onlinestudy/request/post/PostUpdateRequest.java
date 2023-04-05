package seong.onlinestudy.request.post;

import lombok.Data;
import seong.onlinestudy.enumtype.PostCategory;

import java.util.List;

@Data
public class PostUpdateRequest {

    private String title;
    private String content;
    private PostCategory category;
    private List<Long> studyIds;
}
