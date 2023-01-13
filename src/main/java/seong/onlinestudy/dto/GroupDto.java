package seong.onlinestudy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupCategory;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class GroupDto {
    private Long groupId;
    private String name;
    private int headcount;
    private Long memberCount;
    private GroupCategory category;
    private List<GroupMemberDto> groupMembers;
    private List<RoomDto> rooms;

    public static GroupDto from(Group group) {
        GroupDto groupDto = new GroupDto();
        groupDto.groupId = group.getId();
        groupDto.name = group.getName();
        groupDto.headcount = group.getHeadcount();
        groupDto.memberCount = (long) group.getGroupMembers().size();
        groupDto.category = group.getCategory();
        groupDto.groupMembers = group.getGroupMembers().stream()
                .map(GroupMemberDto::from).collect(Collectors.toList());
        groupDto.rooms = group.getRooms().stream()
                .map(RoomDto::from).collect(Collectors.toList());

        return groupDto;
    }
}
