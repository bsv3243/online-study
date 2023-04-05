package seong.onlinestudy.request.group;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import seong.onlinestudy.enumtype.GroupCategory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class GroupCreateRequest {

    @NotNull(message = "그룹명은 2자 이상, 20자 이하여야 합니다.")
    @Size(min = 2, max = 20, message = "그룹명은 2자 이상, 20자 이하여야 합니다.")
    private String name;

    @NotNull(message = "그룹명은 2자 이상, 20자 이하여야 합니다.")
    @Range(min = 1, max = 30, message = "인원수는 최대 30명까지 가능합니다.")
    private int headcount;

    @NotNull(message = "카테고리는 필수입니다.")
    private GroupCategory category;
}
