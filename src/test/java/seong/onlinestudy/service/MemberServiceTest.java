package seong.onlinestudy.service;

import org.apache.logging.log4j.util.StringBuilders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.exception.DuplicateElementException;
import seong.onlinestudy.repository.GroupMemberRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.request.member.MemberUpdateRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    MemberService memberService;

    @Mock
    MemberRepository memberRepository;

    @Spy
    PasswordEncoder passwordEncoder;

    public MemberServiceTest() {
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
    void createMember_중복체크() {
        //given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("아이디"); request.setNickname("닉네임");
        request.setPassword("password"); request.setPasswordCheck("password123");

        Member testMember = Member.createMember(request);
        ReflectionTestUtils.setField(testMember, "id", 1L);

        given(memberRepository.findByUsername(request.getUsername())).willReturn(Optional.of(testMember));

        //when
        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(DuplicateElementException.class);

        //then
    }

    @Test
    void createMember_패스워드체크() {
        //given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("아이디"); request.setNickname("닉네임");
        request.setPassword("password"); request.setPasswordCheck("password123");

        Member testMember = Member.createMember(request);
        ReflectionTestUtils.setField(testMember, "id", 1L);

        //when
        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(IllegalArgumentException.class);

        //then

    }

    @Test
    void updateMember_패스워드체크() {
        //given
        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setNickname("닉네임");
        request.setPasswordOld("passwordOld");
        request.setPasswordNew("passwordNew"); request.setPasswordNewCheck("passwordNew");

        Member testMember = MyUtils.createMember("member", "passwordNotEq");
        ReflectionTestUtils.setField(testMember, "id", 1L);

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(testMember));

        //when
        assertThatThrownBy(() -> memberService.updateMember(1L, request))
                .isInstanceOf(IllegalArgumentException.class);

        //then
    }
    @Test
    void updateMember_패스워드_null() {
        //given
        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setNickname("닉네임");

        Member testMember = MyUtils.createMember("member", "passwordNotEq");
        ReflectionTestUtils.setField(testMember, "id", 1L);

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(testMember));

        //when
        Long updateMemberId = memberService.updateMember(1L, request);

        //then
    }
}