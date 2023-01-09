package seong.onlinestudy.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.exception.BadPasswordException;
import seong.onlinestudy.exception.MemberNotFoundException;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.LoginRequest;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    private final String SECRET = "0e5e67e9-a609-4cf1-9d96-529c1bf883a5";

    public String createToken(LoginRequest loginRequest) {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));

        if (!member.getPassword().equals(password)) {
            throw new BadPasswordException("비밀번호가 일치하지 않습니다.");
        }

        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() +
                        1000 * 60 * 30))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        return token;
    }

    public Member login(LoginRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));

        if(!member.getPassword().equals(password)) {
            throw new BadPasswordException("비밀번호가 일치하지 않습니다.");
        }

        return member;
    }
}
