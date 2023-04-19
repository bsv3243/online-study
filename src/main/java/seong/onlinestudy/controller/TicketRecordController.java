package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.request.record.RecordsGetRequest;
import seong.onlinestudy.service.TicketRecordService;

import javax.validation.Valid;

import java.util.List;

import static seong.onlinestudy.constant.SessionConst.LOGIN_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TicketRecordController {

    private final TicketRecordService ticketRecordService;

    @GetMapping("/records")
    public Result<List<StudyRecordDto>> getRecords(@Valid RecordsGetRequest request,
                                                   @SessionAttribute(value = LOGIN_MEMBER, required = false) Long loginMemberId) {
        List<StudyRecordDto> records = ticketRecordService.getRecords(request, loginMemberId);

        return new Result<>("200", records);
    }

}
