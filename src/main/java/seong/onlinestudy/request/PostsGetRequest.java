package seong.onlinestudy.request;

import lombok.Data;
import seong.onlinestudy.domain.PostCategory;

import java.util.List;

@Data
public class PostsGetRequest {

    private int page;
    private int size;
    private Long groupId;
    private String search;
    private PostCategory category;
    private List<Long> studyIds;
    private boolean deleted;

    public PostsGetRequest() {
        page = 0;
        size = 10;
        deleted = false;
    }
}
