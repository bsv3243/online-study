package seong.onlinestudy.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Size;

@Data
public class GroupCreateRequest {

    @Size(min = 2, max = 30)
    private String name;

    @Range(min = 1, max = 30)
    private int headcount;
}
