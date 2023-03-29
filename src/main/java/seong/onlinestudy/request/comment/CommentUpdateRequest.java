package seong.onlinestudy.request.comment;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CommentUpdateRequest {

    @NotNull(message = "내용이 없습니다.")
    private String content;
}
