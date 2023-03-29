package seong.onlinestudy.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.dto.GroupStudyDto;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.repository.GroupMemberRepository;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.request.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    GroupRepository groupRepository;
    @Mock
    StudyRepository studyRepository;
    @Mock
    GroupMemberRepository groupMemberRepository;
    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    GroupService groupService;


    @Test
    @DisplayName("그룹 가입")
    void joinGroup() {
        //given
        Member master = createMember("memberA", "test1234");
        Member memberA = createMember("memberB", "test1234");

        GroupMember groupMember = GroupMember.createGroupMember(master, GroupRole.MASTER);
        Group group = createGroup("test", 30, groupMember);

        given(memberRepository.findById(any())).willReturn(Optional.of(memberA));
        given(groupRepository.findById(1L)).willReturn(Optional.of(group));

        //when
        Long groupId = groupService.joinGroup(1L, memberA);

        //then
        Group findGroup = groupRepository.findById(1L).get();
        assertThat(findGroup).isEqualTo(group);
        assertThat(findGroup.getGroupMembers().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("그룹 생성")
    void createGroup() {
        //given
        Member member = createMember("memberA", "test1234");
        GroupCreateRequest request = getGroupCreateRequest("groupA", 30);

        given(memberRepository.findById(any())).willReturn(Optional.of(member));

        //when
        groupService.createGroup(request, member);

        //then
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup() {
        //given
        Member master = createMember("memberA", "test1234");
        Member memberA = createMember("memberB", "test1234");

        GroupMember groupMember = GroupMember.createGroupMember(master, GroupRole.MASTER);
        GroupMember groupMember2 = GroupMember.createGroupMember(memberA, GroupRole.USER);

        Group group = createGroup("test", 30, groupMember);
        group.addGroupMember(groupMember2);
        setField(master, "id", 1L);
        setField(memberA, "id", 2L);


        given(groupRepository.findById(1L)).willReturn(Optional.of(group));

        //when
        Long groupId = 1L;
        groupService.deleteGroup(groupId, master);

        //then
    }

    @Test
    @DisplayName("그룹 삭제_권한 없음")
    void deleteGroup_권한없음() {
        //given
        Member master = createMember("memberA", "test1234");
        Member memberA = createMember("memberB", "test1234");

        GroupMember groupMember = GroupMember.createGroupMember(master, GroupRole.MASTER);
        GroupMember groupMember2 = GroupMember.createGroupMember(memberA, GroupRole.USER);

        Group group = createGroup("test", 30, groupMember);
        group.addGroupMember(groupMember2);
        setField(master, "id", 1L);
        setField(memberA, "id", 2L);

        given(groupRepository.findById(1L)).willReturn(Optional.of(group));

        //when
        Long groupId = 1L;

        //then
        assertThatThrownBy(() -> groupService.deleteGroup(groupId, memberA))
                .isInstanceOf(PermissionControlException.class);
    }

    @Test
    @DisplayName("그룹 조회")
    void 그룹조회() {
        //given


        GroupsGetRequest request = new GroupsGetRequest();
        request.setPage(0); request.setSize(10);

        List<Member> testMembers = createMembers(20, true);
        List<Group> testGroups = createGroups(testMembers, 3, true);

        List<GroupMember> testGroupMasters = testGroups.stream()
                .map(group -> group.getGroupMembers().get(0))
                .collect(Collectors.toList());

        joinMembersToGroups(testMembers, testGroups);


        List<Study> testStudies = createStudies(4, true);
        List<GroupStudyDto> testGroupStudyDtos = getGroupStudyDtosRandomOwn(testGroups, testStudies);

        PageImpl<Group> testGroupsWtihPage = new PageImpl<>(testGroups, PageRequest.of(0, 5), 3);

        given(groupRepository.findGroups(any(), any(), any(), any(), any()))
                .willReturn(testGroupsWtihPage);
        given(studyRepository.findStudiesInGroups(any()))
                .willReturn(testGroupStudyDtos);
        given(groupMemberRepository.findGroupMasters(any())).willReturn(testGroupMasters);


        //when
        Page<GroupDto> findGroupsWithPage = groupService.getGroups(request);

        //then
        List<GroupDto> groupDtos = findGroupsWithPage.getContent();
        assertThat(groupDtos.size()).isEqualTo(testGroups.size());
        assertThat(groupDtos).allSatisfy(groupDto -> {
            Group testTargetGroup = getTestTargetGroup(testGroups, groupDto);

            assertThat(testTargetGroup).isNotNull();
            assertThat(groupDto.getGroupMembers()).allSatisfy(groupMemberDto -> {
                assertThat(groupMemberDto.getRole()).isEqualTo(GroupRole.MASTER);
            });
            assertThat(groupDto.getStudies().size()).isGreaterThan(0);
            assertThat(groupDto.getStudies()).allSatisfy(groupStudyDto -> {
                assertThat(groupStudyDto.getGroupId()).isEqualTo(groupDto.getGroupId());
            });
        });
    }

    private List<GroupStudyDto> getTestTargetGroupStudyDtos(List<GroupStudyDto> testGroupStudyDtos, GroupDto groupDto) {
        return testGroupStudyDtos.stream()
                .filter(groupStudyDto -> groupStudyDto.getGroupId().equals(groupDto.getGroupId()))
                .collect(Collectors.toList());
    }

    private Group getTestTargetGroup(List<Group> testGroups, GroupDto groupDto) {
        return testGroups.stream()
                .filter(testGroup -> testGroup.getId().equals(groupDto.getGroupId()))
                .findFirst().get();
    }

    private List<GroupStudyDto> getGroupStudyDtosRandomOwn(List<Group> testGroups, List<Study> studies) {
        Random random = new Random();
        List<GroupStudyDto> testGroupStudyDtos = new ArrayList<>();
        for (Group group : testGroups) {
            int randomTo = random.nextInt(studies.size()) + 1;
            for(int i = 0; i< randomTo; i++) {
                Study selectedStudy = studies.get(i);
                GroupStudyDto groupStudyDto = new GroupStudyDto(selectedStudy.getId(), group.getId(),
                        selectedStudy.getName(), 1000);

                testGroupStudyDtos.add(groupStudyDto);
            }
        }
        return testGroupStudyDtos;
    }

    @Test
    @DisplayName("그룹 업데이트")
    void updateGroup() {
        //given
        Member member = createMember("member", "member");
        setField(member, "id", 1L);
        Group group = MyUtils.createGroup("group", 30, member);
        setField(group, "id", 1L);
        GroupMember master = group.getGroupMembers().get(0);

        given(groupRepository.findGroupWithMembers(any())).willReturn(Optional.of(group));
        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setDescription("한줄평");
        request.setHeadcount(20);

        //when
        Long groupId = groupService.updateGroup(1L, request, member);

        //then
        assertThat(group.getHeadcount()).isEqualTo(20);
        assertThat(group.getDescription()).isEqualTo("한줄평");
    }

    @Test
    @DisplayName("그룹 업데이트_예외, 잘못된 인원수")
    void updateGroup_인원수예외() {
        //given
        List<Member> members = createMembers(30, true);
        Group group = MyUtils.createGroup("group", 30, members.get(0));
        setField(group, "id", 1L);
        GroupMember master = group.getGroupMembers().get(0);

        for(int i=1; i<30; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            group.addGroupMember(groupMember);
        }

        given(groupRepository.findGroupWithMembers(any())).willReturn(Optional.of(group));
        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setDescription("한줄평");
        request.setHeadcount(20);

        //when
        assertThatThrownBy(() -> groupService.updateGroup(1L, request, members.get(0)))
                .isInstanceOf(IllegalArgumentException.class);
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