package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Study;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class StudyRecordDto {

    private Long studyId;
    private String studyName;
    private int memberCount;

    private List<RecordDto> records = new ArrayList<>();

    @JsonIgnore
    Set<Member> members = new HashSet<>();

    /**
     * members 필드의 사이즈를 memberCount 필드에 지정한다.
     */
    public void updateMemberCount() {
        memberCount = members.size();
    }

    /**
     * RecordDto 의 memberCount 필드를 업데이트하고, records 필드에 추가한다.
     * @param recordDto
     */
    public void addRecord(RecordDto recordDto) {
        recordDto.updateMemberCount();

        records.add(recordDto);
        members.addAll(recordDto.getMemberCounter());
    }

    public static StudyRecordDto from(Study study) {
        StudyRecordDto studyRecordDto = new StudyRecordDto();
        if(study != null) {
            studyRecordDto.studyId = study.getId();
            studyRecordDto.studyName = study.getName();
            studyRecordDto.memberCount = 0;
        }

        return studyRecordDto;
    }
}
