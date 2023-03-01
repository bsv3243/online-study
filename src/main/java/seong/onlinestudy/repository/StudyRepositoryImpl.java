package seong.onlinestudy.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupStudyDto;

import javax.persistence.EntityManager;
import java.util.List;

import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QStudy.study;
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
                        ticket.activeTime.sum().as("studyTime")
                ))
                .from(study)
                .join(study.tickets, ticket)
                .join(ticket.group, group)
                .where(group.in(groups))
                .groupBy(group.id, study.id)
                .orderBy(ticket.activeTime.sum().desc())
                .fetch();
    }
}
