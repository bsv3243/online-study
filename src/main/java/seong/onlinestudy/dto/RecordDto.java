package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.StudyTicket;
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
    private Set<Member> memberCounter = new HashSet<>();

    public void changeFirstStartTimeAndLastEndTime(Ticket ticket) {
        if(isFirstInput()) {
            setStartAndEndTime(ticket);
        } else {
            if (startTime.isAfter(ticket.getStartTime())) {
                startTime = ticket.getStartTime();
            }
            if (ticket.isExpired() && endTime.isBefore(ticket.getTicketRecord().getExpiredTime())) {
                endTime = ticket.getTicketRecord().getExpiredTime();
            }
        }
    }

    private boolean isFirstInput() {
        return startTime == null && endTime == null;
    }

    public void setStartAndEndTime(Ticket ticket) {
        startTime = ticket.getStartTime();

        if(ticket.isExpired()) {
            endTime = ticket.getTicketRecord().getExpiredTime();
        } else {
            endTime = startTime;
        }
    }

    public void updateMemberCount() {
        memberCount = memberCounter.size();
    }

    public void addStudyTime(Ticket ticket) {
        if(ticket instanceof StudyTicket) {
            memberCounter.add(ticket.getMember());

            if(ticket.isExpired()) {
                studyTime += ticket.getTicketRecord().getActiveTime();
            }
        }
    }

    protected RecordDto() {

    }

    public static RecordDto from(LocalDate date) {
        RecordDto recordDto = new RecordDto();
        recordDto.date = date;
        recordDto.memberCount = 0;
        recordDto.studyTime = 0;
        return recordDto;
    }

    public static RecordDto from(Ticket ticket) {
        RecordDto recordDto = new RecordDto();
        recordDto.date = ticket.getDateBySetting();
        recordDto.studyTime = 0;
        recordDto.memberCount = 1;
        recordDto.memberCounter.add(ticket.getMember());

        if(ticket.isExpired()) {
            recordDto.studyTime = ticket.getTicketRecord().getActiveTime();
        }
        recordDto.setStartAndEndTime(ticket);

        return recordDto;
    }
}
