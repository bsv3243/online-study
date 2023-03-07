package seong.onlinestudy.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class MemberDuplicateCheckRequest {

    @NotNull(message = "아이디는 6자 이상, 20자 이하여야 합니다.")
    @Size(min = 6, max = 20, message = "아이디는 6자 이상, 20자 이하여야 합니다.")
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "아이디는 영문과 숫자로 구성되어야 합니다.")
    private String username;
}
