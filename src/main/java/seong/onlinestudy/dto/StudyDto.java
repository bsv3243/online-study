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

    public static StudyDto from(Study study) {
        StudyDto studyDto = new StudyDto();
        studyDto.studyId = study.getId();
        studyDto.name = study.getName();

        return studyDto;
    }
}
