package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.MemberDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.request.member.MemberDuplicateCheckRequest;
import seong.onlinestudy.request.member.MemberUpdateRequest;
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
    public Result<Long> createMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        Long memberId = memberService.createMember(memberCreateRequest);

        return new Result<>("201", memberId);
    }

    @GetMapping("/member/{memberId}")
    public Result<MemberDto> getMember(@PathVariable Long memberId,
                                       @SessionAttribute(value = LOGIN_MEMBER, required = false) Member loginMember) {
        if (loginMember == null) {
            throw new InvalidSessionException("세션이 유효하지 않습니다.");
        }
        if(!memberId.equals(loginMember.getId())) {
            throw new PermissionControlException("권한이 없습니다.");
        }

        MemberDto member = memberService.getMember(memberId);

        return new Result<>("200", member);
    }

    @PostMapping("/members/duplicate")
    public Result<Boolean> duplicateCheck(@RequestBody @Valid MemberDuplicateCheckRequest request) {
        memberService.duplicateCheck(request);

        return new Result<>("200", false);
    }

    @PatchMapping("/member/{memberId}")
    public Result<Long> updateMember(@PathVariable Long memberId, @RequestBody @Valid MemberUpdateRequest request,
                                     @SessionAttribute(value = LOGIN_MEMBER, required = false) Member loginMember) {
        if (loginMember == null) {
            throw new InvalidSessionException("세션이 유효하지 않습니다.");
        }
        if(!memberId.equals(loginMember.getId())) {
            throw new PermissionControlException("권한이 없습니다.");
        }

        Long updatedMemberId = memberService.updateMember(memberId, request);

        return new Result<>("200", updatedMemberId);
    }
    }
}
