package seong.onlinestudy.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Comment;
import seong.onlinestudy.domain.QMember;
import seong.onlinestudy.domain.QPost;

import javax.persistence.EntityManager;

import java.util.List;

import static seong.onlinestudy.domain.QComment.comment;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QPost.post;

public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private JPAQueryFactory query;

    public CommentRepositoryImpl(EntityManager em) {
        query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Comment> findComments(Long memberId, Long postId, Pageable pageable) {
        List<Comment> result = query
                .select(comment)
                .from(comment)
                .join(comment.member, member).fetchJoin()
                .join(comment.post, post).fetchJoin()
                .where(memberIdEq(member, memberId),
                        postIdEq(post, postId),
                        comment.deleted.isFalse())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = query
                .select(comment.id.count())
                .from(comment)
                .where(memberIdEq(comment.member, memberId),
                        postIdEq(comment.post, postId),
                        comment.deleted.isFalse())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchOne();

        return new PageImpl<>(result, pageable, count);
    }

    private BooleanExpression postIdEq(QPost post, Long postId) {
        return postId != null ? post.id.eq(postId) : null;
    }

    private BooleanExpression memberIdEq(QMember member, Long memberId) {
        return memberId != null ? member.id.eq(memberId) : null;
    }
}
