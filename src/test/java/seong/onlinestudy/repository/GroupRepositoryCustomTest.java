package seong.onlinestudy.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.*;

import javax.persistence.EntityManager;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.createStudy;
import static seong.onlinestudy.MyUtils.createTicket;
import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QGroupMember.groupMember;
import static seong.onlinestudy.domain.QRecord.record;
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

    List<Member> members;
    List<Group> groups;
    List<Study> studies;
    List<Ticket> tickets;

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        members = MyUtils.createMembers(50);
        memberRepository.saveAll(members);

        groups = MyUtils.createGroups(members, 20);
        groupRepository.saveAll(groups);
        for(int i=0; i<10; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i + 20), GroupRole.USER);
            groups.get(i).addGroupMember(groupMember);
        }

        studies = MyUtils.createStudies(25);
        studyRepository.saveAll(studies);

        tickets = new ArrayList<>();
        for(int i=0; i<30; i++) {
            Ticket ticket = createTicket(TicketStatus.STUDY, members.get(i), studies.get(i % 25), groups.get(i % 10));
            tickets.add(ticket);

            if(i < 20) {
                ZoneOffset offset = ZoneOffset.of("+09:00");
                setField(ticket.getRecord(), "expiredTime", ticket.getStartTime().plusHours(2));
                setField(ticket.getRecord(), "activeTime",
                        ticket.getRecord().getExpiredTime().toEpochSecond(offset)-ticket.getStartTime().toEpochSecond(offset));
                setField(ticket, "expired", true);
            }
        }
        ticketRepository.saveAll(tickets);
    }

    @Test
    void initTest() {

    }

    @Test
    void getGroups() {
        GroupCategory category = null;
        String search = null;
        List<Long> studyIds = null;
        PageRequest pageable = PageRequest.of(0, 5);

        List<Member> members = MyUtils.createMembers(50);
        memberRepository.saveAll(members);

        List<Group> groups = getGroups(members, 20);
        groupRepository.saveAll(groups);

        List<Study> studies = getStudies(25);
        studyRepository.saveAll(studies);

        List<Ticket> tickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            tickets.add(createTicket(TicketStatus.STUDY, members.get(i), studies.get(i % 25), groups.get(i % 20)));
            if(i<25) {
                TicketUpdateRequest request = new TicketUpdateRequest();
                request.setStatus(TicketStatus.END);

                tickets.get(i).expiredAndUpdateRecord();
            }
        }
        ticketRepository.saveAll(tickets);

        List<Group> findGroups = query
                .selectFrom(group)
                .distinct()
                .leftJoin(group.tickets, ticket)
                .leftJoin(ticket.study, study)
                .where(categoryEq(category), nameContains(search), studyIdsIn(studyIds))
                .fetch();


        Group[] groupArr = new Group[20];
        for(int i=0; i<20; i++) {
            groupArr[i] = groups.get(i);
        }
        assertThat(findGroups).containsExactly(groupArr);
        assertThat(findGroups.size()).isEqualTo(20);
    }

    @Test
    void findGroupsOrderBy() {
        //given
        GroupCategory category = null;
        String search = null;
        List<Long> studyIds = null;

        //when
        OrderBy orderBy = OrderBy.TIME;

        OrderSpecifier<Long> order;
        switch (orderBy) {
            case MEMBERS:
                order = groupMember.count().desc();
                break;
            case TIME:
                order =  record.activeTime.sum().desc();
                break;
            default:
                order = group.id.desc();
        }

        List<Group> findGroups = query
                .select(group)
                .from(group)
                .leftJoin(group.tickets, ticket)
                .join(ticket.record, record)
                .leftJoin(ticket.study, study)
                .join(group.groupMembers, groupMember)
                .where(categoryEq(category), nameContains(search), studyIdsIn(studyIds))
                .groupBy(group.id)
                .orderBy(order)
                .limit(10)
                .offset(0)
                .fetch();


        //then
        if(orderBy != OrderBy.MEMBERS && orderBy != OrderBy.TIME) {
            groups.sort((o1, o2) -> (int) (o2.getId() - o1.getId()));

            assertThat(findGroups).containsAll(groups);
            assertThat(findGroups).containsExactlyElementsOf(groups);
        } else if(orderBy == OrderBy.MEMBERS) {
            assertThat(findGroups.get(0).getGroupMembers().size()).isEqualTo(2);
            assertThat(findGroups.get(findGroups.size()-1).getGroupMembers().size()).isEqualTo(1);
        } else if(orderBy == OrderBy.TIME){
            List<Long> studyTimes = new ArrayList<>();
            for (Group group : findGroups) {
                long studyTime = 0L;
                for (Ticket groupTicket : group.getTickets()) {
                    studyTime += groupTicket.getRecord().getActiveTime();
                }

                studyTimes.add(studyTime);
            }

            for(int i=0; i<findGroups.size()-1; i++) {
                assertThat(studyTimes.get(i)).isGreaterThanOrEqualTo(studyTimes.get(i+1));
            }
        }
    }

    private List<Study> getStudies(int endId) {
        List<Study> studies = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            studies.add(createStudy("테스트스터디"+1));
        }
        return studies;
    }

    private List<Group> getGroups(List<Member> members, int endId) {
        List<Group> groups = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            groups.add(createGroup("테스트그룹" + 1, 30, members.get(i)));
        }
        return groups;
    }

    private BooleanExpression studyIdsIn(List<Long> studyIds) {
        return studyIds != null ? study.id.in(studyIds) : null;
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