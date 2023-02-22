package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.dto.GroupStudyDto;
import seong.onlinestudy.exception.InvalidAuthorizationException;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.request.GroupCreateRequest;

import java.util.*;

import static seong.onlinestudy.domain.GroupRole.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final StudyRepository studyRepository;

    @Transactional
    public Long createGroup(GroupCreateRequest createRequest, Member member) {
        GroupMember groupMember = GroupMember.createGroupMember(member, MASTER);
        Group group = Group.createGroup(createRequest, groupMember);

        groupRepository.save(group);
        log.info("그룹이 생성되었습니다. group={}", group);

        return group.getId();
    }

    @Transactional
    public Long joinGroup(Long groupId, Member member) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        GroupMember groupMember = GroupMember.createGroupMember(member, USER);
        group.addGroupMember(groupMember);
        log.info("멤버 {}가 그룹 {}에 가입하였습니다.", member, group);

        return group.getId();
    }

    public Page<GroupDto> getGroups(int page, int size, GroupCategory category, String search, List<Long> studyIds) {
        Page<Group> groups = groupRepository.findGroups(PageRequest.of(page, size), category, search, studyIds);

        List<GroupStudyDto> groupStudies = studyRepository.findStudiesInGroups(groups.getContent());

        Page<GroupDto> groupDtos = groups.map(group -> {
            GroupDto groupDto = GroupDto.from(group);
            groupDto.setMemberSize(group.getGroupMembers().size());
            groupStudies.forEach(study -> {
                if (study.getGroupId().equals(group.getId()))
                    groupDto.getStudies().add(study);
            });

            return groupDto;
        });

        return groupDtos;
    }

    public GroupDto getGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        GroupDto groupDto = GroupDto.from(group);

        return groupDto;
    }

    public void deleteGroup(Long id, Member loginMember) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        GroupMember master = group.getGroupMembers().stream().filter(groupMember ->
                groupMember.getRole().equals(MASTER)).findFirst().get();
        if(!master.getMember().getId().equals(loginMember.getId())) {
            throw new InvalidAuthorizationException("권한이 없습니다.");
        }

        groupRepository.delete(group);
    }

}
