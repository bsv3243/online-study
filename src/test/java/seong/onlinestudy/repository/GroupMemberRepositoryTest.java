package seong.onlinestudy.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.Member;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GroupMemberRepositoryTest {

    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;

    @Test
    void countMemberInGroups() {
        List<Member> members = MyUtils.createMembers(50);
        memberRepository.saveAll(members);

        List<Group> groups = new ArrayList<>();
        for(int i=0; i<10; i++) {
            groups.add(MyUtils.createGroup("테스트그룹" + i, 30, members.get(i)));
        }
        groupRepository.saveAll(groups);

    }
}