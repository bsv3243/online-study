package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.StudyDto;
import seong.onlinestudy.request.study.StudiesGetRequest;
import seong.onlinestudy.service.StudyService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = StudyController.class)
class StudyControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    StudyService studyService;

    MockHttpSession session;

    public StudyControllerTest() {
        session = new MockHttpSession();
    }

    @Test
    void createStudy() {
    }

    @Test
    void getStudies() {
    }

    @Test
    void getStudies_날짜조건없음() throws Exception {
        //given
        Member member = MyUtils.createMember("member", "member123!");
        session.setAttribute(LOGIN_MEMBER, member);

        StudiesGetRequest request = new StudiesGetRequest();
        request.setDate(null);

        StudyDto studyDto = new StudyDto();
        PageImpl<StudyDto> studyDtosWithPage = new PageImpl<>(List.of(studyDto));

        given(studyService.getStudies(any(), any())).willReturn(studyDtosWithPage);

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }
}