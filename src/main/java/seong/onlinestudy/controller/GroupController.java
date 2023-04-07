package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.controller.response.PageResult;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.group.GroupCreateRequest;
import seong.onlinestudy.request.group.GroupUpdateRequest;
import seong.onlinestudy.request.group.GroupsGetRequest;
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
    @ResponseStatus(value = HttpStatus.CREATED)
    public Result<Long> createGroup(@RequestBody @Valid GroupCreateRequest createRequest,
                                    @SessionAttribute(name = LOGIN_MEMBER, required = false)Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long groupId = groupService.createGroup(createRequest, loginMember);

        return new Result<>("201", groupId);
    }

    @PostMapping("/group/{groupId}/join")
    @ResponseStatus(value = HttpStatus.CREATED)
    public Result<Long> joinGroup(@PathVariable Long groupId,
                          @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long joinedGroupId = groupService.joinGroup(groupId, loginMember);

        return new Result<>("201", joinedGroupId);
    }

    @PostMapping("/group/{groupId}/quit")
    public Result<String> quitGroup(@PathVariable Long groupId,
                                  @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        groupService.quitGroup(groupId, loginMember);

        return new Result<>("200", "ok");
    }

    @GetMapping("/groups")
    public Result<List<GroupDto>> getGroups(@Valid GroupsGetRequest request) {
        Page<GroupDto> groupsWithPageInfo = groupService.getGroups(request);

        return new PageResult<>("200", groupsWithPageInfo.getContent(), groupsWithPageInfo);
    }

    @GetMapping("/group/{id}")
    public Result<GroupDto> getGroup(@PathVariable Long id) {
        GroupDto group = groupService.getGroup(id);

        return new Result<>("200", group);
    }

    @DeleteMapping("/group/{id}")
    public Result<String> deleteGroup(@PathVariable Long id,
                                      @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }
        groupService.deleteGroup(id, loginMember);

        return new Result<>("200", "deleted");
    }

    @PostMapping("/group/{id}")
    public Result<Long> updateGroup(@PathVariable Long id,
                                    @RequestBody @Valid GroupUpdateRequest request,
                                    @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long groupId = groupService.updateGroup(id, request, loginMember);

        return new Result<>("200", groupId);
    }

    @DeleteMapping("/groups")
    public Result<String> deleteGroups(@RequestBody @Valid GroupsDeleteRequest request,
                                       @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        groupService.deleteGroups(request, loginMember);

        return new Result<>("200", "delete groups");
    }

    @DeleteMapping("/groups/quit")
    public Result<String> quitGroups(@RequestBody @Valid GroupsDeleteRequest request,
                                     @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        groupService.quitGroups(request, loginMember);

        return new Result<>("200", "quit groups");
    }
}
