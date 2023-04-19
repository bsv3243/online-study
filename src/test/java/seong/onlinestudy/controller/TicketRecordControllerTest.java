package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.RecordDto;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.service.TicketRecordService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.constant.SessionConst.LOGIN_MEMBER;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDateFormat;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDefaultValue;

@AutoConfigureRestDocs
@WebMvcTest(TicketRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketRecordControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    MockHttpSession session;

    public TicketRecordControllerTest() {
        this.session = new MockHttpSession();
    }

    @MockBean
    TicketRecordService ticketRecordService;

    @Test
    @DisplayName("공부 기록 목록 조회")
    void getRecords() throws Exception {
        //given
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("startDate", "2023-04-06");
        request.add("days", "7");
        request.add("studyId", "1");
        request.add("groupId", "1");
        request.add("memberId", "1");

        Member loginMember = MyUtils.createMember("member", "memberPassword");
        session.setAttribute(LOGIN_MEMBER, 1L);

        RecordDto record = createRecordDto();

        StudyRecordDto studyRecord = createStudyRecordDto(record);

        given(ticketRecordService.getRecords(any(), any())).willReturn(List.of(studyRecord));

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/records")
                .session(session)
                .params(request));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        document("ticket-records-get",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("studyId").description("스터디 엔티티 아이디").optional(),
                                        parameterWithName("groupId").description("그룹 엔티티 아이디").optional(),
                                        parameterWithName("memberId").description("회원 엔티티 아이디").optional(),
                                        parameterWithName("startDate")
                                                .attributes(getDateFormat()).attributes(getDefaultValue("7일 전"))
                                                .description("조회 시작 일자"),
                                        parameterWithName("days")
                                                .attributes(getDefaultValue("7"))
                                                .description("조회 일 수")
                                ),
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),

                                        fieldWithPath("studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                                        fieldWithPath("studyName").type(STRING).description("스터디 이름"),
                                        fieldWithPath("memberCount").type(NUMBER).description("스터디 진행 회원 수"),
                                        fieldWithPath("records").type(ARRAY).description("일자별 공부 기록 목록"),

                                        fieldWithPath("records[].date").type(STRING)
                                                .description("공부 기록 일자"),
                                        fieldWithPath("records[].startTime").type(STRING)
                                                .description("제일 빠른 공부 시작 시간"),
                                        fieldWithPath("records[].endTime").type(STRING)
                                                .description("제일 마지막 공부 종료 시간"),
                                        fieldWithPath("records[].studyTime").type(NUMBER)
                                                .description("해당 일자의 총 공부 시간"),
                                        fieldWithPath("records[].memberCount").type(NUMBER)
                                                .description("해당 일자의 스터디 진행 회원 수")
                                )
                        ));
    }

    private StudyRecordDto createStudyRecordDto(RecordDto record) {
        StudyRecordDto studyRecord = new StudyRecordDto();
        studyRecord.setStudyId(1L);
        studyRecord.setStudyName("스터디");
        studyRecord.setMemberCount(1);
        studyRecord.setRecords(List.of(record));
        return studyRecord;
    }

    private RecordDto createRecordDto() {
        RecordDto record = RecordDto.from(LocalDate.now());
        record.setMemberCount(1);
        record.setStudyTime(0);
        record.setStartTime(LocalDateTime.now().minusHours(1));
        record.setEndTime(LocalDateTime.now().plusHours(1));
        record.setDate(LocalDate.now());
        return record;
    }
}