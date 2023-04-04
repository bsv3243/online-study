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
    public Result<List<StudyDto>> getStudies(@Valid StudiesGetRequest searchCond) {
        Page<StudyDto> studiesWithPageInfo = studyService.getStudies(searchCond);

        return new PageResult<>("200", studiesWithPageInfo.getContent(), studiesWithPageInfo);
    }
}
