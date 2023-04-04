package seong.onlinestudy.request.member;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class MemberUpdateRequest {

    @Size(min = 2, max = 12, message = "닉네임은 2자 이상, 12자 이아혀야 합니다.")
    private String nickname;

    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상, 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{6,20}$",
            message = "비밀번호는 영문, 특수문자, 숫자를 포함하여 8자 이상, 20자 이하여야 합니다.")
    private String password;
}
