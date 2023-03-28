package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.domain.StudyTicket;
import seong.onlinestudy.domain.Ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudyDto {

    private Long studyId;
    private String name;

    private Long studyTime;
    private String startTime;
    private String endTime;

    /**
     * 연관된 Ticket 리스트의 첫번째 startTime, 마지막 endTime, 활성화된 시간을 합산하여 필드에 지정한다.
     * @param study Ticket 과 페치 조인한 Study
     */
    public void setStudyTime(Study study) {
        List<StudyTicket> studyTickets = study.getStudyTickets();

        long studyTime = 0;
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startTime = now;
        LocalDateTime endTime = now;

        for (Ticket ticket : studyTickets) {
            studyTime += ticket.getRecord().getActiveTime();
            startTime = ticket.getStartTime().isBefore(startTime) ? ticket.getStartTime() : startTime;

            if(ticket.isExpired()) {
                endTime = ticket.getRecord().getExpiredTime().isAfter(endTime) ? ticket.getRecord().getExpiredTime() : endTime;
            }
        }

        this.studyTime = studyTime;
        this.startTime = startTime.format(DateTimeFormatter.ISO_DATE_TIME);
        if(endTime.isEqual(now)) {
            this.endTime = this.startTime;
        } else {
            this.endTime = endTime.format(DateTimeFormatter.ISO_DATE_TIME);
        }
    }

    public static StudyDto from(Study study) {
        StudyDto studyDto = new StudyDto();
        studyDto.studyId = study.getId();
        studyDto.name = study.getName();

        return studyDto;
    }
}
