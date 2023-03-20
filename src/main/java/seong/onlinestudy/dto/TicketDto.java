package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;
import seong.onlinestudy.domain.Ticket;

import java.time.format.DateTimeFormatter;

@Data
public class TicketDto {
    private Long ticketId;
    private TicketStatus status;
    private Long activeTime;

    private String startTime;
    private String endTime;
    private boolean expired;

    private StudyDto study;

    public static TicketDto from(Ticket ticket) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.ticketId = ticket.getId();
        ticketDto.status = ticket.getTicketStatus();
        ticketDto.expired = ticket.isExpired();
        ticketDto.activeTime = ticket.getRecord().getActiveTime();

        ticketDto.startTime = ticket.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME);
        if(ticket.isExpired()) {
            ticketDto.endTime = ticket.getRecord().getExpiredTime().format(DateTimeFormatter.ISO_DATE_TIME);
        }

        if(ticket.getStudy() != null) {
            ticketDto.study = StudyDto.from(ticket.getStudy());
        }

        return ticketDto;
    }
}
