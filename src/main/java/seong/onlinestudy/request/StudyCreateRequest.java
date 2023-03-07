package seong.onlinestudy.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class StudyCreateRequest {

    @NotNull(message = "스터디명은 필수입니다.")
    private String name;
}
