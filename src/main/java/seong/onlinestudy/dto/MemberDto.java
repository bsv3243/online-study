package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.Member;

@Data
public class MemberDto {
    private Long memberId;
    private String username;
    private String nickname;

    public static MemberDto from(Member member) {
        MemberDto memberDto = new MemberDto();
        memberDto.memberId = member.getId();
        memberDto.username = member.getUsername();
        memberDto.nickname = member.getNickname();

        return memberDto;
    }
}
