package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupCategory;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.service.GroupService;

import javax.validation.Valid;

import java.util.List;

import static seong.onlinestudy.SessionConst.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/groups")
    public Long createGroup(@RequestBody @Valid GroupCreateRequest createRequest,
                            @SessionAttribute(name = LOGIN_MEMBER)Member loginMember) {

        Long groupId = groupService.createGroup(createRequest, loginMember);

        return groupId;
    }

    @PostMapping("/groups/{groupId}")
    public Long joinGroup(@PathVariable Long groupId,
                          @SessionAttribute(name = LOGIN_MEMBER) Member loginMember) {
        Long joinedGroupId = groupService.joinGroup(groupId, loginMember);

        return joinedGroupId;
    }

    @GetMapping("/groups")
    public Result<List<GroupDto>> getGroups(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(defaultValue = "ALL") GroupCategory category,
                                         @RequestParam(required = false) String search) {
        Page<GroupDto> groups = groupService.getGroups(page, size, category, search);

        Result<List<GroupDto>> result = new Result<>(groups.getContent());
        result.setPageInfo(groups);

        return result;
    }
}
