package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.enumtype.TicketStatus;
import seong.onlinestudy.request.ticket.TicketCreateRequest;
import seong.onlinestudy.request.ticket.TicketGetRequest;
import seong.onlinestudy.service.TicketService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDateFormat;
import static seong.onlinestudy.docs.DocumentFormatGenerator.getDefaultValue;

@AutoConfigureRestDocs
@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    TicketService ticketService;

    MockHttpSession session;

    public TicketControllerTest() {
        this.session = new MockHttpSession();
    }

    @Test
    public void getTickets() throws Exception {
        //given
        TicketGetRequest request = new TicketGetRequest();
        request.setGroupId(1L); request.setStudyId(1L); request.setMemberId(1L);
        request.setDate(LocalDate.now()); request.setDays(1);

        Member member = createMember("member", "member");
        Study study = createStudy("스터디");
        Group group = createGroup("그룹", 30, member);
        setField(member, "id", 1L);
        setField(study, "id", 1L);
        setField(group, "id", 1L);

        Ticket targetTicket = createStudyTicket(member, group, study);
        Ticket expiredStudyTicket = createStudyTicket(member, group, study);
        Ticket expiredRestTicket = createStudyTicket(member, group, study);
        setField(expiredStudyTicket, "id", 1L);
        setField(expiredRestTicket, "id", 2L);
        setField(targetTicket, "id", 3L);

        expireTicket(expiredStudyTicket, 3600);
        expireTicket(expiredRestTicket, 500);

        MemberTicketDto memberTicketDto = MemberTicketDto.from(member, List.of(expiredStudyTicket, expiredRestTicket, targetTicket));

        given(ticketService.getTickets(any())).willReturn(List.of(memberTicketDto));

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/tickets")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("tickets-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("groupId").type(NUMBER).description("그룹 엔티티 아이디").optional(),
                                fieldWithPath("studyId").type(NUMBER).description("스터디 엔티티 아이디").optional(),
                                fieldWithPath("memberId").type(NUMBER).description("회원 엔티티 아이디").optional(),
                                fieldWithPath("date").type(STRING)
                                        .attributes(getDateFormat()).attributes(getDefaultValue("오늘")).description("검색 시작 일자"),
                                fieldWithPath("days").type(NUMBER)
                                        .attributes(getDefaultValue("1")).description("검색 할 일수"),
                                fieldWithPath("page").type(NUMBER)
                                        .attributes(getDefaultValue("0")).description("페이지 번호"),
                                fieldWithPath("size").type(NUMBER).
                                        attributes(getDefaultValue("30")).description("페이지 사이즈")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                ticketsGetFields()
                        )));
    }

    private List<FieldDescriptor> ticketsGetFields() {
        List<FieldDescriptor> description = new ArrayList<>(
                List.of(
                        fieldWithPath("memberId").type(NUMBER).description("회원 엔티티 아이디"),
                        fieldWithPath("nickname").type(STRING).description("회원 닉네임"),
                        fieldWithPath("studyTime").type(NUMBER).description("총 공부 시간(단위: 초)")
                ));

        description.add(subsectionWithPath("activeTicket").type(OBJECT).description("활성화된 티켓"));
        description.addAll(ticketField("activeTicket."));

        description.add(subsectionWithPath("expiredTickets").type(ARRAY).description("만료된 티켓 목록"));
        description.addAll(ticketField("expiredTickets[]."));
        description.add(subsectionWithPath("expiredTickets[].study").type(OBJECT).description("티켓 학습"));
        description.addAll(studyField("expiredTickets[].study."));

        return description;
    }

    @Test
    void getTicket() throws Exception {
        //given
        Member member = createMember("member", "member");
        Study study = createStudy("스터디");
        Group group = createGroup("그룹", 30, member);
        setField(member, "id", 1L);
        setField(study, "id", 1L);
        setField(group, "id", 1L);

        Ticket testTicket = createStudyTicket(member, group, study);
        ReflectionTestUtils.setField(testTicket, "id", 1L);
        TicketDto testTicketDto = TicketDto.from(testTicket);

        given(ticketService.getTicket(anyLong())).willReturn(testTicketDto);

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/ticket/{ticketId}", 1));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("ticket-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("ticketId").description("티켓 엔티티 아이디")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                ticketGetField()
                        )));
    }

    private List<FieldDescriptor> ticketGetField() {
        List<FieldDescriptor> descriptors = new ArrayList<>();
        descriptors.addAll(ticketField(""));
        descriptors.add(fieldWithPath("study").type(OBJECT).description("티켓 학습"));
        descriptors.addAll(studyField("study."));

        return descriptors;
    }

    private List<FieldDescriptor> ticketField(String path) {
        return List.of(
                fieldWithPath(path + "ticketId").type(NUMBER).description("티켓 엔티티 아이디"),
                fieldWithPath(path + "status").type(STRING).description("티켓 상태"),
                fieldWithPath(path + "activeTime").type(NUMBER).description("만료되기까지 시간(단위: 초)"),
                fieldWithPath(path + "startTime").type(STRING).description("학습 시작 시간"),
                fieldWithPath(path + "endTime").type(STRING)
                        .description("학습 종료 시간(expired가 false인 경우 null)").optional(),
                fieldWithPath(path + "expired").type(JsonFieldType.BOOLEAN).description("티켓 만료 여부")
        );
    }

    private List<FieldDescriptor> studyField(String path) {
        return List.of(
                fieldWithPath(path + "studyId").type(NUMBER).description("스터디 엔티티 아이디"),
                fieldWithPath(path + "name").type(STRING).description("스터디 이름")
        );
    }

    @Test
    void createTicket() throws Exception {
        //given
        TicketCreateRequest request = new TicketCreateRequest();
        request.setStudyId(1L);
        request.setGroupId(1L);
        request.setStatus(TicketStatus.STUDY);

        Member testMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        //when
        ResultActions resultActions = mvc.perform(RestDocumentationRequestBuilders.post("/api/v1/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .session(session));

        //then
        resultActions.andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("ticket-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("studyId").type(NUMBER).description("학습 엔티티 아이디"),
                                fieldWithPath("groupId").type(NUMBER).description("그룹 엔티티 아이디"),
                                fieldWithPath("status").type(STRING).description("티켓 상태")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("생성된 티켓 엔티티 아이디")
                        )));
    }

    @Test
    void expiredTicket() throws Exception {
        //given
        Member testMember = createMember("member", "member");
        session.setAttribute(LOGIN_MEMBER, 1L);

        //when
        ResultActions resultActions = mvc.perform(RestDocumentationRequestBuilders.patch("/api/v1/ticket/{ticketId}", 1L)
                .session(session));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("ticket-expire",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("ticketId").description("티켓 엔티티 아이디")
                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("HTTP 상태 코드"),
                                fieldWithPath("data").type(NUMBER).description("만료된 티켓 엔티티 아이디")
                        ))
                );
    }
}