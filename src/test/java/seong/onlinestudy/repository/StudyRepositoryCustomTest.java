package seong.onlinestudy.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;

import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QGroupMember.groupMember;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QTicket.ticket;

@DataJpaTest
public class StudyRepositoryCustomTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    TicketRepository ticketRepository;

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        Member member = createMember("member", "member");
        Group group = createGroup("테스트그룹", 30, member);
        memberRepository.save(member);
        groupRepository.save(group);

        Study study = createStudy("테스트스터디");
        studyRepository.save(study);

        Ticket ticket = createTicket(TicketStatus.STUDY, member, study, group);
        ticketRepository.save(ticket);
    }

    @Test
    void findGroupStudies() {
        //given
        Member member1 = createMember("member1", "member1");
        memberRepository.save(member1);

        Group group1 = groupRepository.findAll().get(0);
        GroupMember groupMember1 = GroupMember.createGroupMember(member1, GroupRole.USER);
        group1 .addGroupMember(groupMember1);

        Study study1 = createStudy("테스트스터디1");
        studyRepository.save(study1);

        Ticket ticket1 = getEndTicket(member1, group1 , study1, 2);
        ticketRepository.save(ticket1);

        //when
        List<Group> groups = query
                .select(group)
                .from(group)
                .join(group.groupMembers, groupMember).fetchJoin()
                .join(groupMember.member, member).fetchJoin()
                .where(categoryEq(null), nameContains(null), studiesIn(null))
                .limit(10)
                .offset(0)
                .fetch();

//        List<Long> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
        List<GroupStudyDto> groupStudies = query
                .select(Projections.constructor(GroupStudyDto.class,
                        study.id,
                        group.id,
                        study.name,
                        ticket.activeTime.sum()
                ))
                .from(study)
                .join(study.tickets, ticket)
                .join(ticket.group, group)
                .where(group.in(groups))
                .groupBy(study.id)
                .limit(10)
                .fetch();

        //then
        assertThat(groups).contains(group1);
    }

    private BooleanExpression studiesIn(List<Study> studies) {
        return studies != null ? study.in(studies) : null;
    }

    private BooleanExpression categoryEq(GroupCategory category) {
        return category != null ? group.category.eq(category) : null;
    }

    private BooleanExpression nameContains(String search) {
        return search != null ? group.name.contains(search) : null;
    }

    @Data
    public static class GroupStudyDto {
        private Long studyId;
        private Long groupId;
        private String name;
        private Long studyTime;

        public GroupStudyDto() {
        }

        public GroupStudyDto(Long studyId, Long groupId, String name, Long studyTime) {
            this.studyId = studyId;
            this.groupId = groupId;
            this.name = name;
            this.studyTime = studyTime;
        }
    }

    private Ticket getEndTicket(Member member, Group group, Study study, long hours) {
        Ticket ticket = createTicket(TicketStatus.STUDY, member, study, group);
        setField(ticket, "endTime", ticket.getStartTime().plusHours(hours));
        ZoneOffset offset = ZoneOffset.of("+09:00");
        setField(ticket, "activeTime",
                ticket.getEndTime().toEpochSecond(offset)-ticket.getStartTime().toEpochSecond(offset));
        setField(ticket, "memberStatus", TicketStatus.END);

        return ticket;
    }
}
