package seong.onlinestudy.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;
import java.util.List;

import static seong.onlinestudy.domain.QComment.comment;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QPost.post;
import static seong.onlinestudy.domain.QPostStudy.postStudy;

public class PostRepositoryImpl implements PostRepositoryCustom {

    JPAQueryFactory query;

    public PostRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Post> findPostsWithComments(Pageable pageable, Long groupId, String search, PostCategory category, List<Long> studyIds, Boolean deleted) {

        OrderSpecifier order = post.createdAt.desc();

        List<Post> posts = query
                .select(post)
                .from(post)
                .leftJoin(post.comments, comment).fetchJoin()
                .leftJoin(post.member, member).fetchJoin()
                .leftJoin(post.postStudies, postStudy)
                .where(groupIdEq(groupId), searchContains(search), categoryEq(category), studyIdIn(studyIds), isDeletedEq(deleted))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .orderBy(order)
                .fetch();

        Long count = query
                .select(post.count())
                .from(post)
                .leftJoin(post.postStudies, postStudy)
                .where(groupIdEq(groupId), searchContains(search), categoryEq(category), studyIdIn(studyIds), isDeletedEq(deleted))
                .fetchOne();


        return new PageImpl<>(posts, pageable, count);
    }

    private BooleanExpression isDeletedEq(boolean isDelete) {
        return post.isDeleted.eq(isDelete);
    }

    private BooleanExpression studyIdIn(List<Long> studyIds) {
        return studyIds != null ? postStudy.study.id.in(studyIds) : null;
    }

    private BooleanExpression categoryEq(PostCategory category) {
        return category != null ? post.category.eq(category) : null;
    }

    private BooleanExpression searchContains(String search) {
        return search != null ? post.title.contains(search) : null;
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return groupId != null ? post.group.id.eq(groupId) : null;
    }
}
