package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupCategory;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.GroupRepositoryImpl;
import seong.onlinestudy.request.GroupCreateRequest;

import java.util.List;
import java.util.NoSuchElementException;

import static seong.onlinestudy.domain.GroupRole.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

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

    public Page<GroupDto> getGroups(int page, int size, GroupCategory category, String search) {
        if(category.equals(GroupCategory.ALL)) {
            category = null;
        }

        Page<GroupDto> groups = groupRepository.getGroups(PageRequest.of(page, size), category, search);

        return groups;
    }
}
