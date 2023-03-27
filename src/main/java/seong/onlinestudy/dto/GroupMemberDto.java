package seong.onlinestudy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.GroupRole;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDto {
    private Long groupMemberId;
    private Long groupId;
    private Long memberId;

    private String username;
    private String nickname;
    private LocalDateTime joinedAt;
    private GroupRole role;

    public static GroupMemberDto from(GroupMember groupMember) {
        GroupMemberDto groupMemberDto = new GroupMemberDto();
        groupMemberDto.groupMemberId = groupMember.getId();
        groupMemberDto.groupId = groupMember.getGroup().getId();
        groupMemberDto.memberId = groupMember.getMember().getId();
        groupMemberDto.username = groupMember.getMember().getUsername();
        groupMemberDto.nickname = groupMember.getMember().getNickname();
        groupMemberDto.joinedAt = groupMember.getJoinedAt();
        groupMemberDto.role = groupMember.getRole();

        return groupMemberDto;
    }
}
