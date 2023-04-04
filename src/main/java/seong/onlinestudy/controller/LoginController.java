package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.request.login.LoginRequest;
import seong.onlinestudy.service.LoginService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Result<String> login(@RequestBody @Valid LoginRequest loginRequest,
                                HttpServletRequest request) {

        Member loginMember = loginService.login(loginRequest);
        HttpSession session = request.getSession();

        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        return new Result<>("200", "login 성공");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public Result<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            session.invalidate();
        }

        return new Result<>("200", "logout 성공");
    }
}
