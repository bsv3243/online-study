package seong.onlinestudy.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.GroupRole;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupMemberDto;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static seong.onlinestudy.MyUtils.*;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;

    @Test
    void findGroupMasters() {
        List<Member> members = new ArrayList<>();
        for(int i=0; i<50; i++) {
            Member member = createMember("testMember" + 1, "testMember" + 1);
            members.add(member);
        }
        memberRepository.saveAll(members);

        List<Group> groups = new ArrayList<>();
        for(int i=0; i<10; i++) {
            Group group = createGroup("테스트그룹" + 1, 30, members.get(i));
            groups.add(group);
        }
        groupRepository.saveAll(groups);

        for(int i=20; i<30; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i-20).addGroupMember(groupMember);
        }

        //when
        List<GroupMemberDto> groupMasters = memberRepository.findGroupMasters(groups);

        //then
        assertThat(groupMasters).allSatisfy(member -> {
            member.getRole().equals(GroupRole.MASTER);
        });
        assertThat(groupMasters.size()).isEqualTo(10);
    }

}