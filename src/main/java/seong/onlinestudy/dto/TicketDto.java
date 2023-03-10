package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;
import seong.onlinestudy.domain.Ticket;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Data
public class TicketDto {
    private Long ticketId;
    private TicketStatus status;
    private Long activeTime;

    private String startTime;
    private String endTime;

    private StudyDto study;

    public static TicketDto from(Ticket ticket) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.ticketId = ticket.getId();
        ticketDto.status = ticket.getTicketStatus();
        ticketDto.activeTime = ticket.getActiveTime();

        ticketDto.startTime = ticket.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME);
        if(ticket.isExpired()) {
            ticketDto.endTime = ticket.getEndTime().format(DateTimeFormatter.ISO_DATE_TIME);
        }

        ticketDto.study = StudyDto.from(ticket.getStudy());

        return ticketDto;
    }
}
