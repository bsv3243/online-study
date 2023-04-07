package seong.onlinestudy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.request.group.GroupCreateRequest;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.enumtype.OrderBy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static seong.onlinestudy.enumtype.GroupRole.*;
import static seong.onlinestudy.domain.QGroup.group;

@Slf4j
@DataJpaTest
class GroupRepositoryTest {

    @Autowired
    GroupRepository groupRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
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

    List<Member> members;
    List<Group> groups;

    @BeforeEach
    void init() {
        members = MyUtils.createMembers(30);
        groups = MyUtils.createGroups(members, 4);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
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
    void getGroups_페이지정보() {
        //given
        PageRequest request = PageRequest.of(0, groups.size());

        //when
        Page<Group> findGroups = groupRepository.findGroups(null, null, null, null, OrderBy.CREATEDAT, request
        );

        //then
        assertThat(findGroups.getContent())
                .containsExactlyInAnyOrderElementsOf(groups);
    }

    @Test
    void deleteAllByMemberId() {
        //given
        Member testMember = members.get(0);

        List<Group> testGroups = MyUtils.createGroups(List.of(testMember), 20);
        groupRepository.saveAll(testGroups);

        //when
        groupRepository.softDeleteAllByMemberIdRoleIsMaster(testMember.getId());
        em.clear();

        //then
        List<Group> findGroups = em.createQuery("select g from Group g where g.id in " +
                        "(select gm.group.id from GroupMember gm" +
                        " where gm.member.id = :memberId)", Group.class)
                .setParameter("memberId", testMember.getId())
                .getResultList();

        assertThat(findGroups).isEmpty();

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