package seong.onlinestudy.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.exception.TokenNotFoundException;
import seong.onlinestudy.repository.MemberRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final String SECRET = "0e5e67e9-a609-4cf1-9d96-529c1bf883a5";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            throw new TokenNotFoundException();
        }
        Cookie authCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("AUTHORIZATION"))
                .findFirst()
                .orElseThrow(TokenNotFoundException::new);

        String encodedToken = authCookie.getValue();
        String token = URLDecoder.decode(encodedToken, StandardCharsets.UTF_8).replace("Barer ", "");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes(StandardCharsets.UTF_8))
                .build().parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        request.setAttribute("username", username);

        String requestURI = request.getRequestURI();
        log.info("REQUEST [{}], [{}]", requestURI, username);

        return true;
    }
}
