package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.service.GroupService;

import javax.validation.Valid;

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
}
