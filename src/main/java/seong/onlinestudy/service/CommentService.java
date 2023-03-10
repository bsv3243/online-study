package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.repository.CommentRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.PostRepository;
import seong.onlinestudy.request.CommentCreateRequest;
import seong.onlinestudy.request.CommentUpdateRequest;

import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Long createComment(CommentCreateRequest request, Member loginMember) {
        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new NoSuchElementException("잘못된 접근입니다."));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));

        Comment comment = Comment.create(request);
        comment.setMemberAndPost(member, post);
        log.info("댓글이 작성되었습니다. postId={}, commentId={}, memberId={}",
                post.getId(), comment.getId(), member.getId());

        return comment.getId();
    }

    @Transactional
    public Long updateComment(Long commentId, CommentUpdateRequest request, Member loginMember) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 댓글입니다."));

        //작성자 정보가 같지 않으면
        if (!comment.getMember().getId().equals(loginMember.getId())) {
            throw new PermissionControlException("댓글 수정 권한이 없습니다.");
        }

        String oldContent = comment.getContent();

        comment.update(request);
        log.info("댓글이 수정되었습니다. commentId={}, oldContent={}, newContent={}",
                comment.getId(), oldContent, comment.getContent());

        return comment.getId();
    }

    @Transactional
    public Long deleteComment(Long commentId, Member loginMember) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 댓글입니다."));

        if(!comment.getMember().getId().equals(loginMember.getId())) {
            throw new PermissionControlException("댓글 삭제 권한이 없습니다.");
        }

        comment.delete();

        return comment.getId();
    }
}
