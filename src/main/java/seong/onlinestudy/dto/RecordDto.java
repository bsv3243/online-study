package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordDto {

    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long studyTime;
    private int memberCount;

    @JsonIgnore
    private Set<Member> members = new HashSet<>();

    public void compareStartAndEndTime(Ticket ticket) {
        if(startTime.isAfter(ticket.getStartTime())) {
            startTime = ticket.getStartTime();
        }
        if(endTime.isBefore(ticket.getEndTime())) {
            endTime = ticket.getEndTime();
        }
    }

    public void setStartAndEndTime(Ticket ticket) {
        startTime = ticket.getStartTime();
        endTime = ticket.getEndTime();
    }

    public void updateMemberCount() {
        memberCount = members.size();
    }

    public void addStudyTime(Ticket ticket) {
        if(ticket.getStudy() == null) {
            return;
        }

        members.add(ticket.getMember());
        studyTime += ticket.getActiveTime();
    }

    protected RecordDto() {

    }

    public static RecordDto from(LocalDate date) {
        RecordDto recordDto = new RecordDto();
        recordDto.date = date;
        recordDto.memberCount = 0;
        return recordDto;
    }

    public static RecordDto from(Ticket ticket) {
        RecordDto recordDto = new RecordDto();
        recordDto.date = ticket.getDateBySetting();
        recordDto.studyTime = ticket.getActiveTime();
        recordDto.memberCount = 1;

        recordDto.members.add(ticket.getMember());

        return recordDto;
    }
}
