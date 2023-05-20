package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.request.record.RecordsGetRequest;

import java.time.LocalDate;
import java.util.*;

@Data
public class StudyRecordDto {

    private Long studyId;
    private String studyName;
    private int memberCount;

    private List<RecordDto> records = new ArrayList<>();

    @JsonIgnore
    Set<Member> members = new HashSet<>();

    /**
     *
     * @param study 지정할 study
     * @param recordsGroupByDate 날짜별로 필터링된 recordDto 목록
     * @param request 시작 날짜와 일수를 가져오기 위한 request 객체
     * @return 시작 날짜부터 정해진 일수까지의 recordDto 를 가진 studyRecordDto를 반환
     */
    public static StudyRecordDto from(Study study,
                                      Map<LocalDate, RecordDto> recordsGroupByDate,
                                      RecordsGetRequest request) {
        StudyRecordDto studyRecordDto = new StudyRecordDto();

        studyRecordDto.studyId = study.getId();
        studyRecordDto.studyName = study.getName();
        studyRecordDto.memberCount = 0;

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = startDate.plusDays(request.getDays());
        for(LocalDate date = request.getStartDate(); date.isBefore(endDate); date=date.plusDays(1)) {
            RecordDto recordDto = recordsGroupByDate.get(date);
            studyRecordDto.addRecord(recordDto);
        }
        studyRecordDto.updateMemberCount();

        return studyRecordDto;
    }

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
}
