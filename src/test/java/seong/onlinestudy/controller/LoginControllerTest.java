package seong.onlinestudy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.LoginRequest;
import seong.onlinestudy.request.MemberCreateRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void init() {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("test1234");
        request.setPassword("test1234!");
        request.setNickname("test");

        Member member = Member.createMember(request);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("로그인 성공")
    void 로그인성공() throws Exception {
        //given
        LoginRequest request = new LoginRequest();
        request.setUsername("test1234");
        request.setPassword("test1234!");

        //when
        mvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

    }

}