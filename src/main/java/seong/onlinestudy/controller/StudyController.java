package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.controller.response.PageResult;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.StudyDto;
import seong.onlinestudy.request.study.StudyCreateRequest;
import seong.onlinestudy.request.study.StudiesGetRequest;
import seong.onlinestudy.service.StudyService;

import javax.validation.Valid;
import java.util.List;

import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/studies")
    public Result<Long> createStudy(@RequestBody @Valid StudyCreateRequest createStudyRequest) {
        Long studyId = studyService.createStudy(createStudyRequest);

        return new Result<>("201", studyId);
    }

    @GetMapping("/studies")
    public Result<List<StudyDto>> getStudies(@Valid StudiesGetRequest searchCond,
                                             @SessionAttribute(value = LOGIN_MEMBER, required = false) Member loginMember) {
        Page<StudyDto> studiesWithPageInfo = studyService.getStudies(searchCond, loginMember);

        return new PageResult<>("200", studiesWithPageInfo.getContent(), studiesWithPageInfo);
    }
}
