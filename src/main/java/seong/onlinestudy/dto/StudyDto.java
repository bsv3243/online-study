package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.Study;

@Data
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
