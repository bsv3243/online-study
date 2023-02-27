package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.PostStudy;

@Data
public class PostStudyDto {

    private Long postStudyId;
    private Long studyId;
    private String name;

    public static PostStudyDto from(PostStudy postStudy) {
        PostStudyDto study = new PostStudyDto();
        study.postStudyId = postStudy.getId();
        study.studyId = postStudy.getStudy().getId();
        study.name = postStudy.getStudy().getName();

        return study;
    }
}
