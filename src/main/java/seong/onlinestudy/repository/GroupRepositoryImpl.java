package seong.onlinestudy.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.OrderBy;

import javax.persistence.EntityManager;
import java.util.List;

import static seong.onlinestudy.domain.QGroup.*;
import static seong.onlinestudy.domain.QGroupMember.groupMember;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QTicket.ticket;

public class GroupRepositoryImpl implements GroupRepositoryCustom{

    private final JPAQueryFactory query;

    public GroupRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Group> findGroups(Pageable pageable, GroupCategory category, String search, List<Long> studyIds, OrderBy orderBy) {

        OrderSpecifier order;
        switch (orderBy) {
            case MEMBERS:
                order = groupMember.count().desc();
                break;
            case TIME:
                order = ticket.activeTime.sum().desc();
                break;
            default:
                order = group.createdAt.desc();
        }

        List<Group> groups = query
                .selectFrom(group)
                .leftJoin(group.tickets, ticket)
                .leftJoin(ticket.study, study)
                .join(group.groupMembers, groupMember)
                .where(categoryEq(category), nameContains(search), studyIdsIn(studyIds))
                .groupBy(group.id)
                .orderBy(order)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long total = query
                .select(group.count())
                .from(group)
                .leftJoin(group.tickets, ticket)
                .leftJoin(ticket.study, study)
                .where(categoryEq(category), nameContains(search), studyIdsIn(studyIds))
                .fetchOne();

        return new PageImpl<>(groups, pageable, total);
    }

    private BooleanExpression studyIdsIn(List<Long> studyIds) {
        return studyIds != null ? study.id.in(studyIds) : null;
    }

    private BooleanExpression categoryEq(GroupCategory category) {
        return category != null ? group.category.eq(category) : null;
    }

    private BooleanExpression nameContains(String search) {
        return search != null ? group.name.contains(search) : null;
    }


}
