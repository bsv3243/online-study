package seong.onlinestudy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.TicketUpdateRequest;
import seong.onlinestudy.service.TicketService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static seong.onlinestudy.MyUtils.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TicketService ticketService;

    MockHttpSession session;
    ObjectMapper mapper;

    public TicketControllerTest() {
        this.session = new MockHttpSession();
        this.mapper = new ObjectMapper();
    }

    @Test
    void updateTicket() throws Exception {
        //given
        Member member = MyUtils.createMember("member", "member");
        Study study = createStudy("study");
        Group group = createGroup("group", 30, member);
        Ticket ticket = createTicket(TicketStatus.STUDY, member, study, group);

        session.setAttribute(SessionConst.LOGIN_MEMBER, member);
        given(ticketService.expireTicket(any(), any(), any())).willReturn(1L);

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
}