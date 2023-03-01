package seong.onlinestudy.request;

import lombok.Data;

@Data
public class CommentCreateRequest {

    private String content;
    private Long postId;
}
