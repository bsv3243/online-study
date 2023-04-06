package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.exception.BadCredentialException;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.login.LoginRequest;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    public Member login(LoginRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialException("아이디/비밀번호가 일치하지 않습니다."));

        if(!member.getPassword().equals(password)) {
            throw new BadCredentialException("아이디/비밀번호가 일치하지 않습니다.");
        }

        return member;
    }
}
