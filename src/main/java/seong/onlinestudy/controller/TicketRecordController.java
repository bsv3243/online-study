package seong.onlinestudy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import seong.onlinestudy.controller.response.Result;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.exception.InvalidSessionException;
import seong.onlinestudy.exception.PermissionControlException;
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
                                                   @SessionAttribute(value = LOGIN_MEMBER, required = false) Long memberId) {
        //요청 조건 중 memberId가 있을 경우 로그인한 회원의 id 와 비교
        if(request.getMemberId() != null) {
            if(memberId == null) {
                throw new InvalidSessionException("세션이 유효하지 않습니다.");
            }

            if(!memberId.equals(request.getMemberId())) {
                throw new PermissionControlException("권한이 없습니다.");
            }
        }

        List<StudyRecordDto> records = ticketRecordService.getRecords(request);

        return new Result<>("200", records);
    }

}
