package seong.onlinestudy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupCategory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class GroupDto {
    private Long groupId;
    private String name;
    private int headcount;
    private int memberSize;
    private LocalDate createdAt;
    private String description;
    private GroupCategory category;
    private List<GroupMemberDto> groupMembers = new ArrayList<>();
    private List<GroupStudyDto> studies = new ArrayList<>();

    public static GroupDto from(Group group) {
        GroupDto groupDto = new GroupDto();
        groupDto.groupId = group.getId();
        groupDto.name = group.getName();
        groupDto.headcount = group.getHeadcount();
        groupDto.description = group.getDescription();
        groupDto.category = group.getCategory();

        return groupDto;
    }
}
