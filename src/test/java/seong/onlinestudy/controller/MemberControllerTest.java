package seong.onlinestudy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.service.MemberService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    MockMvc mvc;

    ObjectMapper mapper;

    @MockBean
    MemberService memberService;

    public MemberControllerTest() {
        this.mapper = new ObjectMapper();
    }

    @Test
    void addMember_Success() throws Exception {
        //given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("test123");
        request.setPassword("test123!");
        request.setNickname("test12");

        given(memberService.addMember(any())).willReturn(1L);
        //when
        mvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        //then

    }

    @Test
    void addMember_비밀번호검증실패() throws Exception {
        //given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername("test123");
        request.setPassword("test123"); //특수문자 미포함
        request.setNickname("test");

        //when
        mvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(rs -> {
                    assertThat(rs.getResolvedException())
                            .isInstanceOf(MethodArgumentNotValidException.class);
                })
                .andDo(print());

        //then
    }

    @Test
    void addMember_모두Null() throws Exception {
        //given
        MemberCreateRequest request = new MemberCreateRequest();

        //when
        mvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(rs -> {
                    assertThat(rs.getResolvedException())
                            .isInstanceOf(MethodArgumentNotValidException.class);
                })
                .andDo(print());

        //then
    }
}