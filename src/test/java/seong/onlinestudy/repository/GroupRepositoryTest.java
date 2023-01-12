package seong.onlinestudy.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static seong.onlinestudy.domain.GroupRole.*;
import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QGroupMember.groupMember;

@Slf4j
@DataJpaTest
class GroupRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    GroupRepository groupRepository;
    @Autowired
    MemberRepository memberRepository;
    EntityManager em;

    @TestConfiguration
    static class TestConfig {

        @PersistenceContext
        EntityManager em;

        @Bean
        JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @BeforeEach
    void init() {
        em = testEntityManager.getEntityManager();

        Member memberA = createMember("memberA", "test1234");
        Member memberB = createMember("memberB", "test1234");
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        GroupMember groupMemberA = GroupMember.createGroupMember(memberA, MASTER);
        GroupMember groupMemberB = GroupMember.createGroupMember(memberB, USER);
        Group groupA = createGroup("test", 30, groupMemberA);
        groupA.addGroupMember(groupMemberB);

        GroupMember groupMemberC = GroupMember.createGroupMember(memberB, MASTER);
        Group groupB = createGroup("groupA", 20, groupMemberC);


        groupRepository.save(groupA);
        groupRepository.save(groupB);

    }

    @Test
    @DisplayName("그룹 생성")
    void createGroup() {
        //given
        Member member = createMember("memberTest", "test1234");
        GroupMember groupMember = GroupMember.createGroupMember(member, MASTER);
        Group group = createGroup("groupTest", 20, groupMember);

        //when
        groupRepository.save(group);

        //then
        Group findGroup = groupRepository.findById(group.getId()).get();
        assertThat(findGroup.getName()).isEqualTo("groupTest");
        assertThat(findGroup.getRooms().size()).isEqualTo(1);
        assertThat(findGroup.getGroupMembers().size()).isEqualTo(1);
    }

    @Test
    void 그룹가입() {
        //given
        Member memberA = createMember("memberA", "test1234");
        GroupMember groupMemberA = GroupMember.createGroupMember(memberA, MASTER);
        Group groupA = createGroup("test", 30, groupMemberA);
        groupRepository.save(groupA);

        Member memberB = createMember("memberB", "test1234");
        GroupMember groupMemberB = GroupMember.createGroupMember(memberB, USER);

        //when
        groupA.addGroupMember(groupMemberB);

        //then
        Group findGroup = groupRepository.findById(groupA.getId()).get();
        assertThat(findGroup.getName()).isEqualTo("test");
        assertThat(findGroup.getGroupMembers().size()).isEqualTo(2);

    }

    @Test
    void getGroupMember() {
        //given

        //when
        Group group = groupRepository.findAll().get(0);

        //then
        log.info("groupMembers={}",group.getGroupMembers());
        List<GroupMember> groupMembers = group.getGroupMembers();
        assertThat(groupMembers.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("그룹 리스트 조회, 인원수를 함께 조회")
    void getGroups() {
        JPAQueryFactory query = new JPAQueryFactory(em);

        //given
        Pageable pageable = PageRequest.of(0, 5);
        GroupCategory category = null;
        String search = null;

        //when
        List<GroupDto> result = query
                .select(Projections.fields(GroupDto.class,
                        group.id, group.name, group.headcount, groupMember.id.count().as("memberCount"), group.category))
                .from(group)
                .join(group.groupMembers, groupMember)
                .where(categoryEq(category), nameContains(search))
                .groupBy(group.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(group.count())
                .from(group)
                .where(categoryEq(category), nameContains(search))
                .fetchOne();


        //then
        GroupDto groupDto = result.get(0);
        log.info("group={}", groupDto);
        assertThat(groupDto.getName()).isEqualTo("test");
        assertThat(groupDto.getMemberCount()).isEqualTo(2);
        assertThat(total).isEqualTo(2);
    }

    @Test
    @DisplayName("그룹 리스트 조회, 인원수를 함께 조회(데이터 없음)")
    void getGroups_데이터없음() {
        JPAQueryFactory query = new JPAQueryFactory(em);

        //given
        Pageable pageable = PageRequest.of(0, 5);
        GroupCategory category = null;
        String search = null;
        groupRepository.deleteAll();

        //when
        List<GroupDto> result = query
                .select(Projections.fields(GroupDto.class,
                        group.id, group.name, group.headcount, groupMember.id.count().as("memberCount"), group.category))
                .from(group)
                .join(group.groupMembers, groupMember)
                .where(categoryEq(category), nameContains(search))
                .groupBy(group.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(group.count())
                .from(group)
                .where(categoryEq(category), nameContains(search))
                .fetchOne();


        //then
        assertThat(result.size()).isEqualTo(0);
        assertThat(total).isEqualTo(0);
    }

    @Test
    @DisplayName("그룹 리스트 조회, 인원수를 함께 조회(검색 테스트)")
    void getGroups_검색() {
        JPAQueryFactory query = new JPAQueryFactory(em);

        //given
        Pageable pageable = PageRequest.of(0, 5);
        GroupCategory category = null;
        String search = "gr";

        //when
        List<GroupDto> result = query
                .select(Projections.fields(GroupDto.class,
                        group.id, group.name, group.headcount, groupMember.id.count().as("memberCount"), group.category))
                .from(group)
                .join(group.groupMembers, groupMember)
                .where(categoryEq(category), nameContains(search))
                .groupBy(group.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(group.count())
                .from(group)
                .where(categoryEq(category), nameContains(search))
                .fetchOne();


        //then
        GroupDto groupDto = result.get(0);
        assertThat(groupDto.getName()).isEqualTo("groupA");
        assertThat(total).isEqualTo(1);
    }

    private BooleanExpression categoryEq(GroupCategory category) {
        return category != null ? group.category.eq(category) : null;
    }

    private BooleanExpression nameContains(String search) {
        return search != null ? group.name.contains(search) : null;
    }

    private Group createGroup(String name, int count, GroupMember groupMember) {
        GroupCreateRequest groupRequest = getGroupCreateRequest(name, count);
        return Group.createGroup(groupRequest, groupMember);
    }


    private Member createMember(String username, String password) {
        MemberCreateRequest memberRequest;
        memberRequest = getMemberCreateRequest(username, password);
        return Member.createMember(memberRequest);
    }

    private GroupCreateRequest getGroupCreateRequest(String name, int count) {
        GroupCreateRequest groupRequest = new GroupCreateRequest();
        groupRequest.setName(name);
        groupRequest.setHeadcount(count);
        return groupRequest;
    }

    private MemberCreateRequest getMemberCreateRequest(String username, String password) {
        MemberCreateRequest memberRequest = new MemberCreateRequest();
        memberRequest.setUsername(username);
        memberRequest.setPassword(password);
        memberRequest.setNickname(username);
        return memberRequest;
    }

}