package seong.onlinestudy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import seong.onlinestudy.domain.QRecord;
import seong.onlinestudy.domain.Ticket;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QRecord.record;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QTicket.ticket;

public class TicketRepositoryImpl implements TicketRepositoryCustom{

    JPAQueryFactory query;

    public TicketRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<Ticket> findTickets(Long studyId, Long groupId, Long memberId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Ticket> findTickets = query
                .selectFrom(ticket)
                .join(ticket.group, group)
                .leftJoin(ticket.study, study).fetchJoin()
                .join(ticket.member, member).fetchJoin()
                .join(ticket.record, record).fetchJoin()
                .where(studyIdEq(studyId), groupIdEq(groupId), memberIdEq(memberId),
                        ticket.startTime.goe(startTime), ticket.startTime.lt(endTime))
                .orderBy(study.id.asc(), ticket.startTime.asc())
                .fetch();

        return findTickets;
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? member.id.eq(memberId) : null;
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return groupId != null ? group.id.eq(groupId) : null;
    }

    private BooleanExpression studyIdEq(Long studyId) {
        return studyId != null ? study.id.eq(studyId) : null;
    }
}
