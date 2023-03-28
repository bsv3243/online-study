package seong.onlinestudy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import seong.onlinestudy.domain.StudyTicket;
import seong.onlinestudy.domain.Ticket;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QRecord.record;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QStudyTicket.studyTicket;
import static seong.onlinestudy.domain.QTicket.ticket;

public class TicketRepositoryImpl implements TicketRepositoryCustom, StudyTicketRepositoryCustom{

    JPAQueryFactory query;

    public TicketRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<Ticket> findTickets(List<Long> memberIds, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Ticket> findTickets = query
                .selectFrom(ticket)
                .join(ticket.group, group)
                .join(ticket.member, member).fetchJoin()
                .join(ticket.record, record).fetchJoin()
                .leftJoin(studyTicket).on(studyTicket.eq(ticket))
                .join(studyTicket.study, study).fetchJoin()
                .where(studyIdEq(studyId), groupIdEq(groupId), memberIdsIn(memberIds),
                        ticket.startTime.goe(startTime), ticket.startTime.lt(endTime))
                .orderBy(study.id.asc(), ticket.startTime.asc())
                .fetch();

        return findTickets;
    }

    @Override
    public List<Ticket> findTickets(Long memberId, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Ticket> findTickets = query
                .selectFrom(ticket)
                .join(ticket.group, group)
                .join(ticket.member, member).fetchJoin()
                .join(ticket.record, record).fetchJoin()
                .leftJoin(studyTicket).on(studyTicket.eq(ticket))
                .join(studyTicket.study, study).fetchJoin()
                .where(studyIdEq(studyId), groupIdEq(groupId), memberIdEq(memberId),
                        ticket.startTime.goe(startTime), ticket.startTime.lt(endTime))
                .orderBy(study.id.asc(), ticket.startTime.asc())
                .fetch();

        return findTickets;
    }

    @Override
    public List<StudyTicket> findStudyTickets(Long memberId, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime) {
        return query
                .selectFrom(studyTicket)
                .join(studyTicket.member, member).fetchJoin()
                .join(studyTicket.group, group)
                .join(studyTicket.study, study).fetchJoin()
                .join(studyTicket.record, record).fetchJoin()
                .where(memberIdEq(memberId), groupIdEq(groupId), studyIdEq(studyId),
                        studyTicket.startTime.goe(startTime), studyTicket.startTime.lt(endTime))
                .fetch();
    }

    private BooleanExpression memberIdsIn(List<Long> memberIds) {
        return memberIds != null && memberIds.size() > 0 ? member.id.in(memberIds) : null;
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
