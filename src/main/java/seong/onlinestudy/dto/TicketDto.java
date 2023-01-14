package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.MemberStatus;
import seong.onlinestudy.domain.Ticket;

import java.time.LocalDateTime;

@Data
public class TicketDto {
    private Long ticketId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MemberStatus status;

    private MemberDto member;
    private StudyDto study;
    private RoomDto room;

    public static TicketDto from(Ticket ticket) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.ticketId = ticket.getId();
        ticketDto.startTime = ticket.getStartTime();
        ticketDto.endTime = ticket.getEndTime();
        ticketDto.status = ticket.getMemberStatus();

        ticketDto.member = MemberDto.from(ticket.getMember());
        ticketDto.study = StudyDto.from(ticket.getStudy());
        ticketDto.room = RoomDto.from(ticket.getRoom());

        return ticketDto;
    }
}
