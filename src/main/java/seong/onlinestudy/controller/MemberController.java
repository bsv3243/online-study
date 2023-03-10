package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.MemberDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.request.MemberDuplicateCheckRequest;
import seong.onlinestudy.service.MemberService;

import javax.validation.Valid;

import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

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

    @GetMapping("/member")
    public Result<MemberDto> getMember(@SessionAttribute(value = LOGIN_MEMBER, required = false)Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        MemberDto member = memberService.getMember(loginMember);

        return new Result<>("200", member);
    }

    @PostMapping("/members/duplicate")
    public Result<String> duplicateCheck(@RequestBody @Valid MemberDuplicateCheckRequest request) {
        memberService.duplicateCheck(request);

        return new Result<>("200", "ok");
    }
}
