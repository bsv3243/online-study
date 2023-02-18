package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.TicketDto;
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
    public Result<Long> createTicket(@RequestBody @Valid TicketCreateRequest createTicketRequest,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember) {

        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long ticketId = ticketService.createTicket(createTicketRequest, loginMember);

        return new Result<>("201", ticketId);
    }

    @PostMapping("/tickets/{id}")
    public Result<Long> updateTicket(@PathVariable("id") Long ticketId, TicketUpdateRequest updateTicketRequest,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long updateTicketId = ticketService.updateTicket(ticketId, updateTicketRequest, loginMember);

        return new Result<>("201", updateTicketId);
    }

    @GetMapping("/tickets/{id}")
    public Result<TicketDto> getTicket(@PathVariable("id") Long ticketId) {
        TicketDto ticket = ticketService.getTicket(ticketId);

        return new Result<>("200", ticket);
    }
}
