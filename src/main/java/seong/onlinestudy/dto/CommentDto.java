package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.Comment;

@Data
public class CommentDto {

    private Long commentId;
    private String content;
    private MemberDto member;
    private Long postId;

    public static CommentDto from(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.commentId = comment.getId();
        commentDto.content = comment.getContent();
        commentDto.member = MemberDto.from(comment.getMember());
        commentDto.postId = comment.getPost().getId();

        return commentDto;
    }
}
