package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.service.MemberService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/members")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long addMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        Long memberId = memberService.addMember(memberCreateRequest);

        return memberId;
    }
}
