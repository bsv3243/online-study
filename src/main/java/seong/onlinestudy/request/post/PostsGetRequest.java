package seong.onlinestudy.request.post;

import lombok.Data;
import seong.onlinestudy.enumtype.PostCategory;

import java.util.List;

@Data
public class PostsGetRequest {

    private int page;
    private int size;
    private Long groupId;
    private String search;
    private PostCategory category;
    private List<Long> studyIds;

    public PostsGetRequest() {
        page = 0;
        size = 10;
    }
}
