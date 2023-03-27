package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.request.TicketGetRequest;
import seong.onlinestudy.request.TicketUpdateRequest;
import seong.onlinestudy.service.TicketService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seong.onlinestudy.MyUtils.*;

@AutoConfigureRestDocs
@WebMvcTest(TicketController.class)
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
    void updateTicket() throws Exception {
        //given
        Member member = MyUtils.createMember("member", "member");
        Study study = createStudy("study");
        Group group = createGroup("group", 30, member);
        Ticket ticket = createTicket(TicketStatus.STUDY, member, study, group);

        session.setAttribute(SessionConst.LOGIN_MEMBER, member);
        given(ticketService.expireTicket(any(), any())).willReturn(1L);

        //when
        TicketUpdateRequest request = new TicketUpdateRequest();
        request.setStatus(TicketStatus.END);
        mvc.perform(post("/api/v1/ticket/1")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andDo(print());

        //then
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

        Ticket targetTicket = createTicket(TicketStatus.STUDY, member, study, group);
        Ticket expiredStudyTicket = createTicket(TicketStatus.STUDY, member, study, group);
        Ticket expiredRestTicket = createTicket(TicketStatus.REST, member, study, group);
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
                                fieldWithPath("groupId").type(JsonFieldType.NUMBER).description("그룹 엔티티 아이디").optional(),
                                fieldWithPath("studyId").type(JsonFieldType.NUMBER).description("스터디 엔티티 아이디").optional(),
                                fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("회원 엔티티 아이디").optional(),
                                fieldWithPath("date").type(JsonFieldType.STRING).description("검색 시작 일자(yyyy-MM-dd)(기본값: 오늘)"),
                                fieldWithPath("days").type(JsonFieldType.NUMBER).description("검색 할 일수(기본값: 1)"),
                                fieldWithPath("page").type(JsonFieldType.NUMBER).description("페이지 번호(기본값: 0)"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 사이즈(기본값: 30)")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("회원 엔티티 아이디"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("회원 닉네임"),
                                subsectionWithPath("activeTicket").type(JsonFieldType.OBJECT).description("활성화된 티켓"),
                                fieldWithPath("expiredTickets").type(JsonFieldType.ARRAY).description("만료된 티켓 목록"),
                                fieldWithPath("expiredTickets[].ticketId").type(JsonFieldType.NUMBER).description("티켓 엔티티 아이디"),
                                fieldWithPath("expiredTickets[].status").type(JsonFieldType.STRING).description("티켓 상태"),
                                fieldWithPath("expiredTickets[].activeTime").type(JsonFieldType.NUMBER).description("만료되기까지 시간(단위: 초)"),
                                fieldWithPath("expiredTickets[].startTime").type(JsonFieldType.STRING).description("학습 시작 시간"),
                                fieldWithPath("expiredTickets[].endTime").type(JsonFieldType.STRING).description("학습 종료 시간"),
                                fieldWithPath("expiredTickets[].expired").type(JsonFieldType.BOOLEAN).description("티켓 만료 여부"),
                                fieldWithPath("expiredTickets[].study").type(JsonFieldType.OBJECT).description("티켓 학습").optional(),
                                fieldWithPath("expiredTickets[].study.studyId").type(JsonFieldType.NUMBER).description("스터디 엔티티 아이디").optional(),
                                fieldWithPath("expiredTickets[].study.name").type(JsonFieldType.STRING).description("스터디 이름").optional(),
                                fieldWithPath("studyTime").type(JsonFieldType.NUMBER).description("총 공부 시간(단위: 초)")
                        )));

    }
}