package seong.onlinestudy.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupStudyDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QRecord.record;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QStudyTicket.studyTicket;
import static seong.onlinestudy.domain.QTicket.ticket;

public class StudyRepositoryImpl implements StudyRepositoryCustom{

    private final JPAQueryFactory query;

    public StudyRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }


    @Override
    public List<GroupStudyDto> findStudiesInGroups(List<Group> groups) {
        return query
                .select(Projections.constructor(GroupStudyDto.class,
                        study.id,
                        group.id,
                        study.name,
                        studyTicket.record.activeTime.sum().as("studyTime")
                ))
                .from(study)
                .join(study.studyTickets, studyTicket)
                .join(studyTicket.record, record)
                .join(studyTicket.group, group)
                .where(group.in(groups))
                .groupBy(group.id, study.id)
                .orderBy(record.activeTime.sum().desc())
                .fetch();
    }

    @Override
    public Page<Study> findStudies(Long memberId, Long groupId, String search, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        List<Study> result = query
                .selectFrom(study)
                .leftJoin(study.studyTickets, studyTicket)
                .where(
                        memberIdEq(studyTicket.member, memberId),
                        groupIdEq(studyTicket.group, groupId),
                        studyNameContains(search),
                        studyTicket.startTime.goe(startTime),
                        studyTicket.startTime.lt(endTime)
                )
                .groupBy(study.id)
                .orderBy(studyTicket.record.activeTime.sum().desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = query
                .select(study.id.count())
                .from(study)
                .leftJoin(study.studyTickets, studyTicket)
                .join(studyTicket.member, member)
                .where(
                        memberIdEq(studyTicket.member, memberId),
                        groupIdEq(studyTicket.group, groupId),
                        studyNameContains(search),
                        studyTicket.startTime.goe(startTime),
                        studyTicket.startTime.lt(endTime)
                )
                .fetchOne();


        return new PageImpl<>(result, pageable, count);
    }

    private BooleanExpression studyNameContains(String search) {
        return search != null && !search.isBlank() ? study.name.contains(search) : null;
    }

    private BooleanExpression groupIdEq(QGroup group, Long groupId) {
        return groupId != null ? group.id.eq(groupId) : null;
    }

    private BooleanExpression memberIdEq(QMember member, Long memberId) {
        return memberId != null ? member.id.eq(memberId) : null;
    }
}
