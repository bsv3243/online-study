package seong.onlinestudy.request.comment;

import lombok.Data;

@Data
public class CommentsGetRequest {

    private Long memberId;
    private Long postId;

    private int page;
    private int size;

    public CommentsGetRequest() {
        page = 0;
        size = 30;
    }
}
