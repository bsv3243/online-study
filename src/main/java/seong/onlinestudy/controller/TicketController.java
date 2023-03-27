package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.request.TicketGetRequest;
import seong.onlinestudy.service.TicketService;

import javax.validation.Valid;
import java.util.List;

import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/tickets")
    public Result<List<MemberTicketDto>> getTickets(@Valid TicketGetRequest ticketGetRequest) {

        List<MemberTicketDto> memberTickets = ticketService.getTickets(ticketGetRequest);

        return new Result<>("200", memberTickets);
    }

    @PostMapping("/tickets")
    public Result<Long> createTicket(@RequestBody @Valid TicketCreateRequest createTicketRequest,
                             @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {

        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long ticketId = ticketService.createTicket(createTicketRequest, loginMember);

        return new Result<>("201", ticketId);
    }

    @PostMapping("/ticket/{id}")
    public Result<Long> expiredTicket(@PathVariable("id") Long ticketId,
                                      @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long updateTicketId = ticketService.expireTicket(ticketId, loginMember);

        return new Result<>("201", updateTicketId);
    }

    @GetMapping("/ticket/{id}")
    public Result<TicketDto> getTicket(@PathVariable("id") Long ticketId) {
        TicketDto ticket = ticketService.getTicket(ticketId);

        return new Result<>("200", ticket);
    }
}
