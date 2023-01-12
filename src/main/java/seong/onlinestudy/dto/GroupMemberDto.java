package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.GroupMember;
import seong.onlinestudy.domain.GroupRole;

import java.time.LocalDate;

@Data
public class GroupMemberDto {
    private Long groupMemberId;
    private Long memberId;

    private String username;
    private String nickname;
    private LocalDate joinedAt;
    private GroupRole role;

    public static GroupMemberDto from(GroupMember groupMember) {
        GroupMemberDto groupMemberDto = new GroupMemberDto();
        groupMemberDto.groupMemberId = groupMember.getId();
        groupMemberDto.memberId = groupMember.getMember().getId();
        groupMemberDto.username = groupMember.getMember().getUsername();
        groupMemberDto.nickname = groupMember.getMember().getNickname();
        groupMemberDto.joinedAt = groupMember.getJoinedAt();
        groupMemberDto.role = groupMember.getRole();

        return groupMemberDto;
    }
}
