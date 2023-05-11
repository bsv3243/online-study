package seong.onlinestudy.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.QGroup;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static seong.onlinestudy.domain.QGroupMember.groupMember;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QStudyTicket.studyTicket;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory query;

    public MemberRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Member> findMembersOrderByStudyTime(Long memberId, Long groupId,
                                                    LocalDateTime startTime, LocalDateTime endTime,
                                                    Pageable pageable) {
        List<Member> result = query
                .select(member)
                .from(member)
                .leftJoin(member.groupMembers, groupMember)
                .leftJoin(studyTicket).on(studyTicket.member.eq(member))
                .where(memberIdEq(memberId), groupIdEq(groupMember.group, groupId),
                        ticketStartTimeGoe(startTime), ticketStartTimeLt(endTime))
                .groupBy(member)
                .orderBy(studyTicket.ticketRecord.activeTime.sum().desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long total = query
                .select(member.id.count())
                .from(member)
                .leftJoin(member.groupMembers, groupMember)
                .leftJoin(studyTicket).on(studyTicket.member.eq(member))
                .where(memberIdEq(memberId), groupIdEq(groupMember.group, groupId),
                        ticketStartTimeGoe(startTime), ticketStartTimeLt(endTime))
                .fetchOne();

        return new PageImpl<>(result, pageable, total);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? member.id.eq(memberId) : null;
    }

    private BooleanExpression groupIdEq(QGroup group, Long groupId) {
        return groupId != null ? group.id.eq(groupId) : null;
    }

    private BooleanExpression ticketStartTimeGoe(LocalDateTime startTime) {
        return startTime != null ? studyTicket.startTime.goe(startTime) : null;
    }

    private BooleanExpression ticketStartTimeLt(LocalDateTime endTime) {
        return endTime != null ? studyTicket.startTime.lt(endTime) : null;
    }
}
