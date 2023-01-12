package seong.onlinestudy.test;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QGroupMember.groupMember;
import static seong.onlinestudy.domain.QMember.member;

@DataJpaTest
public class QueryDslTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    JPAQueryFactory query;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;

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
        Member memberA = createMember("memberA", "test1234");
        Member memberB = createMember("memberB", "test1234");
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        GroupMember groupMemberA = GroupMember.createGroupMember(memberA, GroupRole.MASTER);
        Group group = createGroup("test", 30, groupMemberA);
        groupRepository.save(group);

    }

    @Test
    void queryDsl_조회() {

        //when
        Group group = query
                .select(QGroup.group)
                .from(QGroup.group)
                .where(QGroup.group.name.eq("test"))
                .fetchOne();

        //then
        assertThat(group.getName()).isEqualTo("test");
        assertThat(group.getGroupMembers().size()).isEqualTo(1);
    }

    @Test
    void queryDsl_카운트() {


        //when
        Long count = query
                .select(member.count())
                .from(member)
                .fetchOne();

        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void queryDsl_조인() {
        //given
        Member member = memberRepository.findByUsername("memberB").get();
        GroupMember newGroupMember = GroupMember.createGroupMember(member, GroupRole.USER);
        Group findGroup = groupRepository.findAll().get(0);
        findGroup.addGroupMember(newGroupMember);
        testEntityManager.flush();

        //when
        List<GroupDto> result = query
                .select(Projections.fields(GroupDto.class,
                        group.id, group.name, group.headcount, groupMember.id.count().as("memberCount")))
                .from(group)
                .join(group.groupMembers, groupMember)
                .groupBy(group.id)
                .fetch();

        //then
        GroupDto groupDto = result.get(0);
        assertThat(groupDto.getName()).isEqualTo("test");
        assertThat(groupDto.getMemberCount()).isEqualTo(2);
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
