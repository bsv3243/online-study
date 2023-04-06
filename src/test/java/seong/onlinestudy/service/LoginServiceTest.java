package seong.onlinestudy.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.exception.BadCredentialException;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.login.LoginRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @InjectMocks
    LoginService loginService;

    @Mock
    MemberRepository memberRepository;

    @Spy
    PasswordEncoder passwordEncoder;

    public LoginServiceTest() {
        passwordEncoder = new MockPasswordEncoder();
    }

    static class MockPasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return new StringBuilder(rawPassword).reverse().toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }

    @Test
    void login_아이디불일치() {
        //given
        LoginRequest request = new LoginRequest();
        request.setUsername("member"); request.setPassword("password");

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialException.class);
    }

    @Test
    void login_비밀번호불일치() {
        //given
        LoginRequest request = new LoginRequest();
        request.setUsername("member"); request.setPassword("password");

        Member member = MyUtils.createMember("member", "passwordNotEq");

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        //when
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialException.class);
    }
}