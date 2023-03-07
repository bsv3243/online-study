package seong.onlinestudy.controller;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.SessionConst;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupCategory;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.OrderBy;
import seong.onlinestudy.service.GroupService;

import javax.validation.Valid;

import java.util.List;

import static seong.onlinestudy.SessionConst.*;

@Api(tags = "GroupController")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성(인증)")
    @PostMapping("/groups")
    public Result<Long> createGroup(@RequestBody @Valid GroupCreateRequest createRequest,
                            @SessionAttribute(name = LOGIN_MEMBER, required = false)Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long groupId = groupService.createGroup(createRequest, loginMember);

        return new Result<>("201", groupId);
    }

    @Operation(summary = "그룹 가입(인증)")
    @PostMapping("/group/{groupId}/join")
    public Result<Long> joinGroup(@PathVariable Long groupId,
                          @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }

        Long joinedGroupId = groupService.joinGroup(groupId, loginMember);

        return new Result<>("201", joinedGroupId);
    }

    @Operation(summary = "그룹 리스트 반환")
    @GetMapping("/groups")
    public Result<List<GroupDto>> getGroups(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) GroupCategory category,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) List<Long> studyIds,
                                            @RequestParam(defaultValue = "CREATEDAT")OrderBy orderBy) {
        Page<GroupDto> groups = groupService.getGroups(page, size, category, search, studyIds, orderBy);

        Result<List<GroupDto>> result = new Result<>("200", groups.getContent());
        result.setPageInfo(groups);

        return result;
    }

    @Operation(summary = "그룹 1개 반환")
    @GetMapping("/group/{id}")
    public Result<GroupDto> getGroup(@PathVariable Long id) {
        GroupDto group = groupService.getGroup(id);

        return new Result<>("200", group);
    }

    @Operation(summary = "그룹 삭제(인증)")
    @DeleteMapping("/group/{id}")
    public Result<String> deleteGroup(@PathVariable Long id,
                                      @SessionAttribute(name = LOGIN_MEMBER, required = false) Member loginMember) {
        if(loginMember == null) {
            throw new InvalidSessionException("세션 정보가 유효하지 않습니다.");
        }
        groupService.deleteGroup(id, loginMember);

        return new Result<>("200", "deleted");
    }
}
