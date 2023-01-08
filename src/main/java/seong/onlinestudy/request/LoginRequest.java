package seong.onlinestudy.request;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class LoginRequest {

    @Size(min = 6, max = 20)
    private String username;

    @Size(min = 6, max = 20)
    private String password;
}
