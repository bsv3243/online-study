package seong.onlinestudy.request.comment;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CommentCreateRequest {

    @NotNull(message = "내용이 없습니다.")
    private String content;

    @NotNull(message = "게시글이 지정되지 않았습니다.")
    private Long postId;
}
