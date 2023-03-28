package seong.onlinestudy.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.MyUtils.*;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    TicketRepository ticketRepository;

    List<Member> testMembers;
    List<Group> testGroups;
    List<Study> testStudies;
    List<Ticket> testStudyTickets;
    List<Ticket> testRestTickets;

    @BeforeEach
    void init() {
        testMembers = createMembers(10);
        testGroups = createGroups(testMembers, 2);
        testStudies = createStudies(2);
        joinMembersToGroups(testMembers, testGroups);

        testStudyTickets = createStudyTickets(testMembers, testGroups, testStudies);
        testRestTickets = createStudyTickets(testMembers, testGroups, testStudies);

        memberRepository.saveAll(testMembers);
        groupRepository.saveAll(testGroups);
        studyRepository.saveAll(testStudies);
        ticketRepository.saveAll(testStudyTickets);
        ticketRepository.saveAll(testRestTickets);
    }

    @Test
    void initTest() {
        List<Ticket> testTickets = new ArrayList<>(testStudyTickets);
        testTickets.addAll(testRestTickets);

        List<Ticket> findTickets = ticketRepository.findAll();

        assertThat(findTickets).containsExactlyInAnyOrderElementsOf(findTickets);
    }

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

    }

    @Test
    void findMembersInGroup() {
        //given
        List<Member> members = createMembers(10);
        memberRepository.saveAll(members);

        Group group = createGroup("group", 30, members.get(0));
        groupRepository.save(group);

        for(int i=1; i<5; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            group.addGroupMember(groupMember);
        }

        //when
        List<Member> membersInGroup = memberRepository.findMembersInGroup(group.getId());

        //then
        assertThat(membersInGroup).containsExactlyInAnyOrderElementsOf(members.subList(0, 5));
    }

    @Test
    public void findMembersOrderByStudyTime() {
        //given
        Member testMember = testMembers.get(5);
        List<Ticket> testTickets = testMember.getTickets();
        for (Ticket ticket : testTickets) {
            expireTicket(ticket, 3600);
        }

        //when
        PageRequest pageRequest = PageRequest.of(0, 1);
        LocalDateTime now = LocalDateTime.now();
        Page<Member> findMembersWithPage = memberRepository
                .findMembersOrderByStudyTime(now.minusMinutes(1), now.plusMinutes(1), pageRequest);

        //then
        List<Member> content = findMembersWithPage.getContent();
        assertThat(content.size()).isEqualTo(1);
        assertThat(content.get(0)).isEqualTo(testMember);
    }
}