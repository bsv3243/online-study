package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.domain.TicketStatus;

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
    private Set<Member> memberCounter = new HashSet<>();

    public void compareStartAndEndTime(Ticket ticket) {
        if(startTime.isAfter(ticket.getStartTime())) {
            startTime = ticket.getStartTime();
        }
        if(ticket.isExpired() && endTime.isBefore(ticket.getRecord().getExpiredTime())) {
            endTime = ticket.getRecord().getExpiredTime();
        }
    }

    public void setStartAndEndTime(Ticket ticket) {
        startTime = ticket.getStartTime();
        endTime = ticket.getRecord().getExpiredTime();
    }

    public void updateMemberCount() {
        memberCount = memberCounter.size();
    }

    public void addStudyTime(Ticket ticket) {
        if(ticket.getTicketStatus().equals(TicketStatus.Values.STUDY)) {
            memberCounter.add(ticket.getMember());
            studyTime += ticket.getRecord().getActiveTime();
        }
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
        recordDto.studyTime = ticket.getRecord().getActiveTime();
        recordDto.memberCount = 1;

        recordDto.memberCounter.add(ticket.getMember());

        recordDto.setStartAndEndTime(ticket);

        return recordDto;
    }
}
