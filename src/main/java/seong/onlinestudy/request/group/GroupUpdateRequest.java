package seong.onlinestudy.request.group;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Size;

@Data
public class GroupUpdateRequest {

    @Size(max = 20, message = "그룹 설명은 최대 100자까지 가능합니다.")
    private String description;

    @Range(min = 1, max = 30, message = "인원수는 최대 30명까지 가능합니다.")
    private Integer headcount;
}
