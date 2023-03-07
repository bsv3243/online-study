package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.request.MemberDuplicateCheckRequest;
import seong.onlinestudy.service.MemberService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/members")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Result<Long> addMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        Long memberId = memberService.addMember(memberCreateRequest);

        return new Result<>("201", memberId);
    }

    @PostMapping("/members/duplicate")
    public Result<String> duplicateCheck(@RequestBody @Valid MemberDuplicateCheckRequest request) {
        memberService.duplicateCheck(request);

        return new Result<>("200", "ok");
    }
}
