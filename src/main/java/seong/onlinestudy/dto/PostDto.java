package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.domain.PostCategory;
import seong.onlinestudy.domain.PostStudy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PostDto {

    private Long postId;
    private String title;
    private String content;
    private PostCategory category;
    private MemberDto member;
    private GroupDto group;
    private List<PostStudyDto> postStudies = new ArrayList<>();
    private List<CommentDto> comments = new ArrayList<>();

    public static PostDto from(Post post) {
        PostDto postDto = new PostDto();
        postDto.postId = post.getId();
        postDto.title = post.getTitle();
        postDto.content = post.getContent();
        postDto.category = post.getCategory();
        postDto.member = MemberDto.from(post.getMember());

        if(post.getGroup() != null) {
            postDto.group = GroupDto.from(post.getGroup());
        }

        postDto.comments = post.getComments().stream()
                .map(CommentDto::from).collect(Collectors.toList());

        return postDto;
    }
}
