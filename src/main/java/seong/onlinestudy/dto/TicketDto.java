package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;
import seong.onlinestudy.domain.Ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDto {
    private Long ticketId;
    private TicketStatus status;
    private Long activeTime;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean expired;

    private StudyDto study;

    public static TicketDto from(Ticket ticket) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.ticketId = ticket.getId();
        ticketDto.status = ticket.getTicketStatus();
        ticketDto.expired = ticket.isExpired();
        ticketDto.activeTime = ticket.getRecord().getActiveTime();

        ticketDto.startTime = ticket.getStartTime();
        if(ticket.isExpired()) {
            ticketDto.endTime = ticket.getRecord().getExpiredTime();
        }

        if(ticket.getStudy() != null) {
            ticketDto.study = StudyDto.from(ticket.getStudy());
        }

        return ticketDto;
    }
}
