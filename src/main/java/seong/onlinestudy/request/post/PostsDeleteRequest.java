package seong.onlinestudy.request.post;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PostsDeleteRequest {

    @NotNull(message = "회원 아이디는 필수입니다.")
    private Long MemberId;
}
