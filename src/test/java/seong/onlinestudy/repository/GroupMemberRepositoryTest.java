package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.enumtype.GroupRole;
import seong.onlinestudy.domain.Member;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        List<GroupMember> result = groupMemberRepository.findGroupMasters(groups);

        //then
        assertThat(result.size()).isEqualTo(masters.size());
    }
}