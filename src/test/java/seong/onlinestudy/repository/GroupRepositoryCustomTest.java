package seong.onlinestudy.repository;

import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.request.StudyCreateRequest;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QGroupMember.groupMember;
import static seong.onlinestudy.domain.QMember.member;
import static seong.onlinestudy.domain.QStudy.study;
import static seong.onlinestudy.domain.QTicket.ticket;

@Slf4j
@DataJpaTest
class GroupRepositoryCustomTest {

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
        Member member1 = createMember("test1234", "test1234");
        Group group1 = createGroup("테스트그룹", 30, member1);

        memberRepository.save(member1);
        groupRepository.save(group1);

        Member member2 = createMember("tester1", "tester1");
        memberRepository.save(member2);

        GroupMember groupMember2 = GroupMember.createGroupMember(member2, GroupRole.USER);
        group1.addGroupMember(groupMember2);

        StudyCreateRequest studyCreateRequest = new StudyCreateRequest();
        studyCreateRequest.setName("테스트스터디");
        Study study = Study.createStudy(studyCreateRequest);

        studyRepository.save(study);

        Ticket ticket = Ticket.createTicket(member1, study, group1);

        ticketRepository.save(ticket);
    }

    @Test
    void initTest() {

    }

    @Test
    void getGroupsWithMembers() {

        //when
        List<Group> groups = query
                .select(group)
                .from(group)
                .join(group.groupMembers, groupMember)
                .fetchJoin()
                .join(groupMember.member, member)
                .fetch();

        //then
        Group group = groups.get(0);
        assertThat(group.getName()).isEqualTo("테스트그룹");

        log.info("groupMembers={}", group.getGroupMembers());
    }

    @Test
    void getGroupsWithStudies() {
        //given
        List<Study> studies = studyRepository.findAll();
        String search = null;
        GroupCategory category = null;

        //when
        List<Group> groups = query
                .selectFrom(group)
                .distinct()
                .join(group.tickets, ticket).fetchJoin()
                .join(ticket.study, study).fetchJoin()
                .where(categoryEq(category), nameContains(search), studiesIn(studies))
                .fetch();

        //then
        Group group = groupRepository.findAll().get(0);
        assertThat(groups).contains(group);
    }

    @Test
    void getGroupsWithStudiesGroupByGroupAndStudy() {
        //given


        List<GroupStudyDto> groupStudies = query
                .select(Projections.constructor(GroupStudyDto.class,
                        study.id,
                        group.id,
                        study.name,
                        ticket.activeTime.sum()
                ))
                .from(group)
                .join(group.tickets, ticket)
                .join(ticket.study, study)
                .groupBy(group.id, study.id)
                .fetch();
    }

    @Data
    static class GroupStudyDto {
        private Long studyId;
        private Long groupId;
        private String name;
        private Long studyTime;
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

    private Group createGroup(String name, int headcount, Member member) {
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName(name);
        request.setHeadcount(headcount);

        GroupMember groupMember = GroupMember.createGroupMember(member, GroupRole.MASTER);

        return Group.createGroup(request, groupMember);
    }

    private Member createMember(String username, String password) {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername(username);
        request.setNickname(username);
        request.setPassword(password);

        return Member.createMember(request);
    }
}