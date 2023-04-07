package seong.onlinestudy.request.comment;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CommentsDeleteRequest {

    @NotNull(message = "회원 아이디는 필수입니다.")
    private Long memberId;
}
