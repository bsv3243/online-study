package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.domain.PostCategory;
import seong.onlinestudy.dto.PostDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.PostCreateRequest;
import seong.onlinestudy.service.PostService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    private final PostService postService;

    @GetMapping("/posts")
    public Result<List<Post>> getPosts(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(required = false) PostCategory category,
                                       @RequestParam(required = false) List<Long> studyIds) {
        postService.getPosts(page, size, search, category, studyIds);

        return null;
    }

    @PostMapping("/posts")
    public Result<Long> createPost(@RequestBody @Valid PostCreateRequest request,
                                   @SessionAttribute(value = SessionConst.LOGIN_MEMBER)Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }
        Long postId = postService.createPost(request, loginMember);

        return new Result<>("201", postId);
    }

    @GetMapping("/post/{postId}")
    public Result<PostDto> getPost(@PathVariable("postId") Long postId) {
        PostDto post = postService.getPost(postId);

        return new Result<>("200", post);
    }
}
