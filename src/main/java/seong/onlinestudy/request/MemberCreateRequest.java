package seong.onlinestudy.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class MemberCreateRequest {

//    @Range(min = 6, max = 20, message = "아이디는 6자 이상, 20자 이하여야 합니다.")
    @Size(min = 6, max = 20, message = "아이디는 6자 이상, 20자 이하여야 합니다.")
    private String username;

    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상, 20자 이하여야 합니다.")
    private String password;

    @Size(min = 2, max = 12, message = "닉네임은 2자 이상, 12자 이아혀야 합니다.")
    private String nickname;
}
