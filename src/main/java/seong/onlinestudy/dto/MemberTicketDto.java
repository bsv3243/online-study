package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.domain.TicketStatus;

import java.util.ArrayList;
import java.util.List;

import static seong.onlinestudy.domain.TicketStatus.END;
import static seong.onlinestudy.domain.TicketStatus.REST;

@Data
public class MemberTicketDto {

    private Long memberId;
    private String nickname;

    private TicketDto activeTicket;
    private List<TicketDto> expiredTickets = new ArrayList<>();

    public static MemberTicketDto from(Member member, List<Ticket> tickets) {
        MemberTicketDto memberTicket = new MemberTicketDto();
        memberTicket.memberId = member.getId();
        memberTicket.nickname = member.getNickname();

        for (Ticket ticket : tickets) {
            TicketDto ticketDto = TicketDto.from(ticket);
            if (ticket.getTicketStatus() == END) {
                memberTicket.expiredTickets.add(ticketDto);
            } else {
                memberTicket.activeTicket = ticketDto;
            }
        }

        return memberTicket;
    }
}
