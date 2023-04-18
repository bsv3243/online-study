package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.controller.response.PageResult;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.dto.CommentDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.CommentsGetRequest;
import seong.onlinestudy.request.comment.CommentCreateRequest;
import seong.onlinestudy.request.comment.CommentUpdateRequest;
import seong.onlinestudy.request.comment.CommentsDeleteRequest;
import seong.onlinestudy.service.CommentService;

import javax.validation.Valid;

import java.util.List;

import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Long> createComment(@RequestBody @Valid CommentCreateRequest request,
                                      @SessionAttribute(value = LOGIN_MEMBER, required = false) Long memberId) {
        if (memberId == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long commentId = commentService.createComment(request, memberId);

        return new Result<>("201", commentId);
    }

    @GetMapping("/comments")
    public Result<List<CommentDto>> getComments(@Valid CommentsGetRequest request) {
        Page<CommentDto> commentsWithPage = commentService.getComments(request);

        return new PageResult<>("200", commentsWithPage.getContent(), commentsWithPage);
    }

    @PatchMapping("/comments/{commentId}")
    public Result<Long> updateComment(@PathVariable("commentId") Long commentId, @RequestBody @Valid CommentUpdateRequest request,
                                      @SessionAttribute(value = LOGIN_MEMBER, required = false) Long memberId) {
        if (memberId == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long updateCommentId = commentService.updateComment(commentId, request, memberId);

        return new Result<>("200", updateCommentId);
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<Long> deleteComment(@PathVariable("commentId") Long commentId,
                                        @SessionAttribute(value = LOGIN_MEMBER, required = false) Long memberId) {
        if (memberId == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long deleteCommentId = commentService.deleteComment(commentId, memberId);

        return new Result<>("200", deleteCommentId);
    }

    @PostMapping("/comments/delete")
    public Result<String> deleteComments(@RequestBody @Valid CommentsDeleteRequest request,
                                         @SessionAttribute(value = LOGIN_MEMBER, required = false) Long memberId) {
        if (memberId == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        commentService.deleteComments(request, memberId);

        return new Result<>("200", "delete comments");
    }
}
