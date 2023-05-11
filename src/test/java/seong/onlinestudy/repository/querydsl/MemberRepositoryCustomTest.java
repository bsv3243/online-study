package seong.onlinestudy.repository.querydsl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.GroupRole;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MemberRepositoryCustomTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    StudyRepository studyRepository;

    List<Member> members;
    List<Group> groups;
    List<Study> studies;

    @BeforeEach
    void init() {
        members = MyUtils.createMembers(30);
        memberRepository.saveAll(members);

        groups = List.of(MyUtils.createGroup("group", 30, members.get(0)));
        groupRepository.saveAll(groups);

        MyUtils.joinMembersToGroups(members.subList(1, 30), groups);

        studies = List.of(MyUtils.createStudy("study"));
        studyRepository.saveAll(studies);
    }

    @Test
    void findMembersOrderByStudy_조건없음() {
        //given
        Long memberId = null;
        Long groupId = null;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        PageRequest pageRequest = PageRequest.of(0, 30);

        //when
        Page<Member> result = memberRepository
                .findMembersOrderByStudyTime(memberId, groupId, startTime, endTime, pageRequest);

        //then
        List<Member> findMembers = result.getContent();
        Assertions.assertThat(findMembers).containsExactlyInAnyOrderElementsOf(members);
    }

    @Test
    public void findMemberOrderByStudy_시간조건있음() {
        //given
        List<Member> testMembers = members.subList(0, 15);

        List<Ticket> studyTickets = MyUtils.createStudyTickets(testMembers, groups, studies, false);
        ticketRepository.saveAll(studyTickets);

        Long memberId = null;
        Long groupId = null;
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        PageRequest pageRequest = PageRequest.of(0, 30);

        //when
        Page<Member> result = memberRepository
                .findMembersOrderByStudyTime(memberId, groupId, startTime, endTime, pageRequest);

        //then
        List<Member> findMembers = result.getContent();
        Assertions.assertThat(findMembers).containsExactlyInAnyOrderElementsOf(testMembers);
    }

    @Test
    public void findMemberOrderByStudy_groupId() {
        //given
        List<Member> testMembers = MyUtils.createMembers(10);
        memberRepository.saveAll(testMembers);

        Group testGroup = MyUtils.createGroup("group2", 30, testMembers.get(0));
        groupRepository.save(testGroup);

        MyUtils.joinMembersToGroups(testMembers.subList(0, 10), List.of(testGroup));

        Long memberId = null;
        Long groupId = testGroup.getId();
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        PageRequest pageRequest = PageRequest.of(0, 30);

        //when
        Page<Member> result = memberRepository
                .findMembersOrderByStudyTime(memberId, groupId, startTime, endTime, pageRequest);

        //then
        List<Member> findMembers = result.getContent();
        Assertions.assertThat(findMembers).containsExactlyInAnyOrderElementsOf(testMembers);
    }

    @Test
    public void findMemberOrderByStudy_memberId() {
        //given
        List<Member> testMembers = MyUtils.createMembers(10);
        memberRepository.saveAll(testMembers);

        Group testGroup = MyUtils.createGroup("group2", 30, testMembers.get(0));
        groupRepository.save(testGroup);

        MyUtils.joinMembersToGroups(testMembers.subList(0, 10), List.of(testGroup));

        Member targetMember = testMembers.get(0);  //target

        Long memberId = targetMember.getId();
        Long groupId = null;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        PageRequest pageRequest = PageRequest.of(0, 30);

        //when
        Page<Member> result = memberRepository
                .findMembersOrderByStudyTime(memberId, groupId, startTime, endTime, pageRequest);

        //then
        List<Member> findMembers = result.getContent();

        Assertions.assertThat(findMembers).containsExactlyInAnyOrderElementsOf(List.of(targetMember));
    }

    @Test
    public void findMemberOrderByStudy_모든조건() {
        //given
        List<Member> testMembers = members.subList(0, 15);

        List<Ticket> studyTickets = MyUtils.createStudyTickets(testMembers, groups, studies, false);
        ticketRepository.saveAll(studyTickets);

        Member targetMember = testMembers.get(0);  //target

        Long memberId = targetMember.getId();
        Long groupId = groups.get(0).getId();
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        PageRequest pageRequest = PageRequest.of(0, 30);

        //when
        Page<Member> result = memberRepository
                .findMembersOrderByStudyTime(memberId, groupId, startTime, endTime, pageRequest);

        //then
        List<Member> findMembers = result.getContent();

        Assertions.assertThat(findMembers).containsExactlyInAnyOrderElementsOf(List.of(targetMember));
    }

}