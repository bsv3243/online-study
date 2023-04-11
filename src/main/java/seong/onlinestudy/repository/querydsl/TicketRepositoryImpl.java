package seong.onlinestudy.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QStudyTicket.studyTicket;
import static seong.onlinestudy.domain.QTicket.ticket;
import static seong.onlinestudy.domain.QTicketRecord.ticketRecord;

public class TicketRepositoryImpl implements TicketRepositoryCustom, StudyTicketRepositoryCustom{

    JPAQueryFactory query;

    public TicketRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<Ticket> findTickets(List<Long> memberIds, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Ticket> findTickets = query
                .selectFrom(ticket)
                .join(ticket.member, member).fetchJoin()
                .join(ticket.ticketRecord, ticketRecord).fetchJoin()
                .leftJoin(studyTicket).on(studyTicket.eq(ticket))
                .where(
                        memberIdsIn(ticket.member, memberIds),
                        groupIdEq(studyTicket.group, groupId),
                        studyIdEq(studyTicket.study, studyId),
                        ticket.startTime.goe(startTime),
                        ticket.startTime.lt(endTime)
                )
                .orderBy(studyTicket.study.id.asc(), ticket.startTime.asc())
                .fetch();

        return findTickets;
    }

    @Override
    public List<Ticket> findTickets(Long memberId, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Ticket> findTickets = query
                .selectFrom(ticket)
                .join(ticket.member, member).fetchJoin()
                .join(ticket.ticketRecord, ticketRecord).fetchJoin()
                .leftJoin(studyTicket).on(studyTicket.eq(ticket))
                .where(
                        memberIdEq(ticket.member, memberId),
                        groupIdEq(ticket.group, groupId),
                        studyIdEq(studyTicket.study, studyId),
                        ticket.startTime.goe(startTime),
                        ticket.startTime.lt(endTime)
                )
                .orderBy(studyTicket.study.id.asc(), ticket.startTime.asc())
                .fetch();

        return findTickets;
    }

    @Override
    public List<StudyTicket> findStudyTickets(Long memberId, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime) {
        return query
                .selectFrom(studyTicket)
                .join(studyTicket.member, member).fetchJoin()
                .join(studyTicket.study, study).fetchJoin()
                .join(studyTicket.ticketRecord, ticketRecord).fetchJoin()
                .where(
                        memberIdEq(studyTicket.member, memberId),
                        groupIdEq(studyTicket.group, groupId),
                        studyIdEq(studyTicket.study, studyId),
                        studyTicket.startTime.goe(startTime),
                        studyTicket.startTime.lt(endTime)
                )
                .fetch();
    }

    private BooleanExpression memberIdsIn(QMember member, List<Long> memberIds) {
        return memberIds != null && memberIds.size() > 0 ? member.id.in(memberIds) : null;
    }

    private BooleanExpression memberIdEq(QMember member, Long memberId) {
        return memberId != null ? member.id.eq(memberId) : null;
    }

    private BooleanExpression groupIdEq(QGroup group, Long groupId) {
        return groupId != null ? group.id.eq(groupId) : null;
    }

    private BooleanExpression studyIdEq(QStudy study, Long studyId) {
        return studyId != null ? study.id.eq(studyId) : null;
    }

}
