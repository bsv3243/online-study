package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.CommentCreateRequest;
import seong.onlinestudy.service.CommentService;

import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public Result<Long> createComment(CommentCreateRequest request,
                                       @SessionAttribute(value = LOGIN_MEMBER)Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long commentId = commentService.createComment(request, loginMember);

        return new Result<>("201", commentId);
    }
}
