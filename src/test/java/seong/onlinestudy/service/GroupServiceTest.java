package seong.onlinestudy.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.GroupRole;
import seong.onlinestudy.exception.InvalidAuthorizationException;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    GroupRepository groupRepository;

    @InjectMocks
    GroupService groupService;


    @Test
    void joinGroup() {
        //given
        Member master = createMember("memberA", "test1234");
        Member memberA = createMember("memberB", "test1234");

        GroupMember groupMember = GroupMember.createGroupMember(master, GroupRole.MASTER);
        Group group = createGroup("test", 30, groupMember);

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
        ReflectionTestUtils.setField(master, "id", 1L);
        ReflectionTestUtils.setField(memberA, "id", 2L);


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
        ReflectionTestUtils.setField(master, "id", 1L);
        ReflectionTestUtils.setField(memberA, "id", 2L);

        given(groupRepository.findById(1L)).willReturn(Optional.of(group));

        //when
        Long groupId = 1L;

        //then
        assertThatThrownBy(() -> groupService.deleteGroup(groupId, memberA))
                .isInstanceOf(InvalidAuthorizationException.class);
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