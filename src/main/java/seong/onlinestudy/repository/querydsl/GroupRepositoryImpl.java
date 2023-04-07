package seong.onlinestudy.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.enumtype.OrderBy;

import javax.persistence.EntityManager;
import java.util.List;

import static seong.onlinestudy.domain.QGroup.*;
import static seong.onlinestudy.domain.QGroupMember.groupMember;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QStudyTicket.studyTicket;
import static seong.onlinestudy.domain.QTicket.ticket;

public class GroupRepositoryImpl implements GroupRepositoryCustom{

    private final JPAQueryFactory query;

    public GroupRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Group> findGroups(Long memberId, GroupCategory category, String search, List<Long> studyIds, OrderBy orderBy, Pageable pageable) {

        OrderSpecifier order;
        switch (orderBy) {
            case MEMBERS:
                order = groupMember.count().desc();
                break;
            case TIME:
                order = ticket.record.activeTime.sum().desc();
                break;
            default:
                order = group.createdAt.desc();
        }

        List<Group> groups = query
                .selectFrom(group)
                .join(group.groupMembers, groupMember)
                .leftJoin(group.tickets, ticket)
                .leftJoin(studyTicket).on(studyTicket.eq(ticket))
                .where(memberIdEq(memberId), categoryEq(category), nameContains(search), studyIdsIn(studyTicket.study, studyIds))
                .groupBy(group.id)
                .orderBy(order)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long total = query
                .select(group.countDistinct())
                .from(group)
                .leftJoin(group.tickets, ticket)
                .leftJoin(studyTicket).on(studyTicket.eq(ticket))
                .where(memberIdEq(memberId), categoryEq(category), nameContains(search), studyIdsIn(studyTicket.study, studyIds))
                .fetchOne();

        return new PageImpl<>(groups, pageable, total);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? groupMember.member.id.eq(memberId) : null;
    }

    private BooleanExpression studyIdsIn(QStudy study, List<Long> studyIds) {
        return studyIds != null && !studyIds.isEmpty() ? study.id.in(studyIds) : null;
    }

    private BooleanExpression categoryEq(GroupCategory category) {
        return category != null ? group.category.eq(category) : null;
    }

    private BooleanExpression nameContains(String search) {
        return search != null ? group.name.contains(search) : null;
    }


}
