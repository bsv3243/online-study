package seong.onlinestudy.dto;

import lombok.Data;

@Data
public class GroupStudyDto {

    private Long studyId;
    private Long GroupId;
    private String name;
    private long studyTime;

    public GroupStudyDto(Long studyId, Long groupId, String name, long studyTime) {
        this.studyId = studyId;
        GroupId = groupId;
        this.name = name;
        this.studyTime = studyTime;
    }
}
