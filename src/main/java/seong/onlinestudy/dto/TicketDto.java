package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.TicketStatus;
import seong.onlinestudy.domain.Ticket;

import java.time.LocalDateTime;

@Data
public class TicketDto {
    private Long ticketId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TicketStatus status;

    private MemberDto member;
    private StudyDto study;

    public static TicketDto from(Ticket ticket) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.ticketId = ticket.getId();
        ticketDto.startTime = ticket.getStartTime();
        ticketDto.endTime = ticket.getEndTime();
        ticketDto.status = ticket.getTicketStatus();

        ticketDto.member = MemberDto.from(ticket.getMember());
        ticketDto.study = StudyDto.from(ticket.getStudy());

        return ticketDto;
    }
}
