package seong.onlinestudy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.enumtype.GroupCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GroupDto {
    private Long groupId;
    private String name;
    private int headcount;
    private int memberSize;
    private boolean deleted;
    private LocalDateTime createdAt;
    private String description;
    private GroupCategory category;
    private List<GroupMemberDto> groupMembers = new ArrayList<>();
    private List<GroupStudyDto> studies = new ArrayList<>();

    public static GroupDto from(Group group) {
        GroupDto groupDto = new GroupDto();
        groupDto.groupId = group.getId();
        groupDto.name = group.getName();
        groupDto.headcount = group.getHeadcount();
        groupDto.deleted = group.isDeleted();
        groupDto.createdAt = group.getCreatedAt();
        groupDto.description = group.getDescription();
        groupDto.category = group.getCategory();

        return groupDto;
    }

    //queryDSL을 위한 생성자입니다.
    //count 쿼리를 받기 위해 memberSize 를 long 타입으로 받습니다.
    public GroupDto(Long groupId, String name, int headcount, long memberSize, boolean deleted,
                    LocalDateTime createdAt, String description, GroupCategory category) {
        this.groupId = groupId;
        this.name = name;
        this.headcount = headcount;
        this.memberSize = (int) memberSize;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.description = description;
        this.category = category;
    }
}
