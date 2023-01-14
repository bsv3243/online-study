package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.request.TicketUpdateRequest;
import seong.onlinestudy.service.TicketService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/tickets")
    public Long createTicket(@RequestBody @Valid TicketCreateRequest createTicketRequest,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember) {

        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long ticketId = ticketService.createTicket(createTicketRequest, loginMember);

        return ticketId;
    }

    @PostMapping("/tickets/{id}")
    public Long updateTicket(@PathVariable("id") Long ticketId, TicketUpdateRequest updateTicketRequest,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long updateTicketId = ticketService.updateTicket(ticketId, updateTicketRequest, loginMember);

        return updateTicketId;
    }
}
