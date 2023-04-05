package seong.onlinestudy.request.group;

import lombok.Data;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.enumtype.OrderBy;

import java.util.List;

@Data
public class GroupsGetRequest {
    private int page;
    private int size;
    private GroupCategory category;
    private String search;
    private List<Long> studyIds;
    private OrderBy orderBy;

    public GroupsGetRequest() {
        page = 0;
        size = 12;
        orderBy = OrderBy.CREATEDAT;
    }
}
