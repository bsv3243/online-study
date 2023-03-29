package seong.onlinestudy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import seong.onlinestudy.domain.RestTicket;
import seong.onlinestudy.domain.StudyTicket;
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
        ticketDto.expired = ticket.isExpired();
        ticketDto.activeTime = ticket.getRecord().getActiveTime();
        ticketDto.startTime = ticket.getStartTime();
        if(ticket.isExpired()) {
            ticketDto.endTime = ticket.getRecord().getExpiredTime();
        }

        if(ticket instanceof StudyTicket) {
            ticketDto.status = TicketStatus.STUDY;

            StudyTicket studyTicket = (StudyTicket) ticket;
            ticketDto.study = StudyDto.from(studyTicket.getStudy());
        } else if(ticket instanceof RestTicket) {
            ticketDto.status = TicketStatus.REST;
        }

        return ticketDto;
    }
}
