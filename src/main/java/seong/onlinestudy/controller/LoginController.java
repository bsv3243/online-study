package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.request.LoginRequest;
import seong.onlinestudy.service.LoginService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public String login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {

        String token = "Barer " + loginService.createToken(loginRequest);
        Cookie cookie = new Cookie("AUTHORIZATION", URLEncoder.encode(token, StandardCharsets.UTF_8));
        cookie.setHttpOnly(true);

        log.info("토큰이 발급되었습니다. username={}", loginRequest.getUsername());
        response.addCookie(cookie);

        return "ok";
    }
}
