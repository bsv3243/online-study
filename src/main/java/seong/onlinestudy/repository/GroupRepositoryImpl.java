package seong.onlinestudy.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupCategory;
import seong.onlinestudy.domain.QGroupMember;
import seong.onlinestudy.dto.GroupDto;

import javax.persistence.EntityManager;
import java.util.List;

import static seong.onlinestudy.domain.QGroup.*;
import static seong.onlinestudy.domain.QGroupMember.*;

public class GroupRepositoryImpl implements GroupRepositoryCustom{

    private final JPAQueryFactory query;

    public GroupRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<GroupDto> getGroups(Pageable pageable, GroupCategory category, String search) {
        List<GroupDto> content = query
                .select(Projections.fields(GroupDto.class,
                        group.id, group.name, group.headcount, groupMember.id.count().as("memberCount"), group.category))
                .from(group)
                .join(group.groupMembers, groupMember)
                .where(categoryEq(category), nameContains(search))
                .groupBy(group.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(group.count())
                .from(group)
                .where(categoryEq(category), nameContains(search))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression categoryEq(GroupCategory category) {
        return category != null ? group.category.eq(category) : null;
    }

    private BooleanExpression nameContains(String search) {
        return search != null ? group.name.contains(search) : null;
    }

}
