package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.GroupRole;
import seong.onlinestudy.domain.Member;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@DataJpaTest
class GroupMemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;

    @Test
    void countMemberInGroups() {
        List<Member> members = createMembers(50);
        memberRepository.saveAll(members);

        List<Group> groups = new ArrayList<>();
        for(int i=0; i<10; i++) {
            groups.add(MyUtils.createGroup("테스트그룹" + i, 30, members.get(i)));
        }
        groupRepository.saveAll(groups);

    }

    @Test
    @DisplayName("그룹 목록의 그룹장 리스트를 조회")
    void findGroupMasters() {
        //given
        List<Member> masters = createMembers(10);
        List<Member> members = createMembers(10, 30);
        memberRepository.saveAll(masters);
        memberRepository.saveAll(members);

        List<Group> groups = createGroups(masters, 10);
        groupRepository.saveAll(groups);

        List<GroupMember> groupMembers = new ArrayList<>();
        for (Member member : members) {
            groupMembers.add(GroupMember.createGroupMember(member, GroupRole.USER));
        }
        for(int i=0; i<members.size(); i++) {
            groups.get(i%groups.size()).addGroupMember(groupMembers.get(i));
        }

        //when
//        List<GroupMember> result = em.createQuery("select gm from GroupMember gm" +
//                        " join fetch gm.group g" +
//                        " join fetch gm.member m" +
//                        " where gm.role='MASTER' and g in :groups", GroupMember.class)
//                .setParameter("groups", groups)
//                .getResultList();
        List<GroupMember> result = groupMemberRepository.findGroupMasters(groups);

        //then
        assertThat(result.size()).isEqualTo(masters.size());
    }
}