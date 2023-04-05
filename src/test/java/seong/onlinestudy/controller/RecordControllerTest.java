package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.docs.DocumentFormatGenerator;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.RecordDto;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.request.record.RecordsGetRequest;
import seong.onlinestudy.service.RecordService;

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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDateFormat;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDefaultValue;

@WebMvcTest(RecordController.class)
@AutoConfigureRestDocs
class RecordControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    MockHttpSession session;

    public RecordControllerTest() {
        this.session = new MockHttpSession();
    }

    @MockBean
    RecordService recordService;

    @Test
    @DisplayName("공부 기록 목록 조회")
    void getRecords() throws Exception {
        //given
        RecordsGetRequest request = new RecordsGetRequest();
        request.setStartDate(LocalDate.now()); request.setDays(7);
        request.setStudyId(1L); request.setGroupId(1L); request.setMemberId(1L);

        Member loginMember = MyUtils.createMember("member", "memberPassword");
        session.setAttribute(LOGIN_MEMBER, loginMember);

        RecordDto record = createRecordDto();

        StudyRecordDto studyRecord = createStudyRecordDto(record);

        given(recordService.getRecords(any(), any())).willReturn(List.of(studyRecord));

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/records")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        document("records-get",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("studyId").type(NUMBER)
                                                .description("스터디 엔티티 아이디").optional(),
                                        fieldWithPath("groupId").type(NUMBER)
                                                .description("그룹 엔티티 아이디").optional(),
                                        fieldWithPath("memberId").type(NUMBER)
                                                .description("회원 엔티티 아이디").optional(),
                                        fieldWithPath("startDate").type(STRING)
                                                .attributes(getDateFormat()).attributes(getDefaultValue("7일 전"))
                                                .description("조회 시작 일자"),
                                        fieldWithPath("days").type(NUMBER)
                                                .attributes(getDefaultValue("7"))
                                                .description("조회 일 수")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                        fieldWithPath("data").type(ARRAY).description("공부 기록 목록"),

                                        fieldWithPath("data[].studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                                        fieldWithPath("data[].studyName").type(STRING).description("스터디 이름"),
                                        fieldWithPath("data[].memberCount").type(NUMBER).description("스터디 진행 회원 수"),
                                        fieldWithPath("data[].records").type(ARRAY).description("일자별 공부 기록 목록"),

                                        fieldWithPath("data[].records[].date").type(STRING)
                                                .description("공부 기록 일자"),
                                        fieldWithPath("data[].records[].startTime").type(STRING)
                                                .description("제일 빠른 공부 시작 시간"),
                                        fieldWithPath("data[].records[].endTime").type(STRING)
                                                .description("제일 마지막 공부 종료 시간"),
                                        fieldWithPath("data[].records[].studyTime").type(NUMBER)
                                                .description("해당 일자의 총 공부 시간"),
                                        fieldWithPath("data[].records[].memberCount").type(NUMBER)
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