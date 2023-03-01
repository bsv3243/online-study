package seong.onlinestudy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberCountDto {

    private Long groupId;
    private Long memberCount;
}
