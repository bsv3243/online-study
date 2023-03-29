package seong.onlinestudy.request.post;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {

    private String title;
    private String content;
    private List<Long> studyIds;
}
