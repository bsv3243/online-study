package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.controller.response.PageResult;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.PostCategory;
import seong.onlinestudy.dto.PostDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.PostCreateRequest;
import seong.onlinestudy.request.PostUpdateRequest;
import seong.onlinestudy.request.PostsGetRequest;
import seong.onlinestudy.service.PostService;

import javax.validation.Valid;
import java.util.List;

import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    private final PostService postService;

    @GetMapping("/posts")
    public Result<List<PostDto>> getPosts(@Valid PostsGetRequest request) {
        Page<PostDto> postsWithPageInfo = postService.getPosts(request);

        return new PageResult<>("200", postsWithPageInfo.getContent(), postsWithPageInfo);
    }

    @PostMapping("/posts")
    public Result<Long> createPost(@RequestBody @Valid PostCreateRequest request,
                                   @SessionAttribute(value = LOGIN_MEMBER, required = false)Member loginMember) {
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

    @PostMapping("/post/{postId}")
    public Result<Long> updatePost(@PathVariable("postId") Long postId,
                                   @RequestBody @Valid PostUpdateRequest request,
                                   @SessionAttribute(value = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }
        Long updatePostId = postService.updatePost(postId, request, loginMember);

        return new Result<>("200", updatePostId);
    }

    @PatchMapping("/post/{postId}")
    public Result<String> deletePost(@PathVariable("postId") Long postId,
                                   @SessionAttribute(value = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }
        postService.deletePost(postId, loginMember);

        return new Result<>("200", "delete post");
    }
}
