package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.request.RecordsGetRequest;
import seong.onlinestudy.service.RecordService;

import javax.validation.Valid;

import java.util.List;

import static seong.onlinestudy.SessionConst.LOGIN_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/records")
    public Result<List<StudyRecordDto>> getRecords(@Valid RecordsGetRequest request,
                             @SessionAttribute(value = LOGIN_MEMBER, required = false) Member loginMember) {
        List<StudyRecordDto> records = recordService.getRecords(request, loginMember);

        return new Result<>("200", records);
    }

}
