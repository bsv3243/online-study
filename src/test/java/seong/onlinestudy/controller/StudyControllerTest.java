package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import seong.onlinestudy.dto.StudyDto;
import seong.onlinestudy.request.study.StudyCreateRequest;
import seong.onlinestudy.service.StudyService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDateFormat;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDefaultValue;

@AutoConfigureRestDocs
@WebMvcTest(controllers = StudyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(RestDocumentationExtension.class)
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

    @BeforeEach
    void init(WebApplicationContext context, RestDocumentationContextProvider provider) {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(provider))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    void createStudy() throws Exception {
        //given
        StudyCreateRequest request = new StudyCreateRequest();
        request.setName("스터디");

        given(studyService.createStudy(any())).willReturn(1L);

        //when
        ResultActions resultActions = mvc.perform(RestDocumentationRequestBuilders.post("/api/v1/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("study-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").type(STRING).description("스터디명")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("생성된 스터디 엔티티 아이디")
                        )));
    }

    @Test
    void getStudies() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("page", "0");
        request.add("size", "10");
        request.add("name", "스터디명");
        request.add("memberId", "1");
        request.add("groupId", "1");
        request.add("date", "2023-04-06");
        request.add("days", "7");

        StudyDto studyDto = new StudyDto();
        studyDto.setStudyId(1L); studyDto.setName("스터디명");
        PageImpl<StudyDto> studyDtosWithPage = new PageImpl<>(List.of(studyDto));

        given(studyService.getStudies(any())).willReturn(studyDtosWithPage);

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/studies")
                .params(request));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("studies-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("name").description("스터디명").optional(),
                                parameterWithName("memberId").description("회원 엔티티 아이디").optional(),
                                parameterWithName("groupId").description("그룹 엔티티 아이디").optional(),
                                parameterWithName("date").attributes(getDateFormat())
                                        .description("검색 시작 날짜").optional(),
                                parameterWithName("days").description("검색 일수(범위)").optional(),
                                parameterWithName("page").attributes(getDefaultValue("0"))
                                        .description("페이지 번호"),
                                parameterWithName("size").attributes(getDefaultValue("10")).
                                        description("페이지 사이즈")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                                fieldWithPath("name").type(STRING).description("스터디명")
                        )));
    }
}