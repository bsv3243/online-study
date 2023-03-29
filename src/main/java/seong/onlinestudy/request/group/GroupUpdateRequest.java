package seong.onlinestudy.request.group;

import lombok.Data;

@Data
public class GroupUpdateRequest {

    private String description;
    private Integer headcount;
}
