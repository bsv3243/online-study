package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import seong.onlinestudy.dto.StudyDto;
import seong.onlinestudy.request.StudyCreateRequest;
import seong.onlinestudy.request.StudySearchCond;
import seong.onlinestudy.service.StudyService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/studies")
    public Long createStudy(@RequestBody @Valid StudyCreateRequest createStudyRequest) {
        Long studyId = studyService.createStudy(createStudyRequest);

        return studyId;
    }

    @GetMapping("/studies")
    public Result<List<StudyDto>> getStudies(@Valid StudySearchCond searchCond) {
        Page<StudyDto> studies = studyService.getStudies(searchCond);
        Result<List<StudyDto>> result = new Result<>("200", studies.getContent());
        result.setPageInfo(studies);

        return result;
    }
}
