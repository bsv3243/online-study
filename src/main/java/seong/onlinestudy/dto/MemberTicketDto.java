package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.StudyTicket;
import seong.onlinestudy.domain.Ticket;

import java.util.ArrayList;
import java.util.List;

@Data
public class MemberTicketDto {

    private Long memberId;
    private String nickname;

    private TicketDto activeTicket;
    private List<TicketDto> expiredTickets = new ArrayList<>();
    private long studyTime;

    public static MemberTicketDto from(Member member, List<Ticket> tickets) {
        MemberTicketDto memberTicket = new MemberTicketDto();
        memberTicket.memberId = member.getId();
        memberTicket.nickname = member.getNickname();
        memberTicket.studyTime = 0;

        for (Ticket ticket : tickets) {
            TicketDto ticketDto = TicketDto.from(ticket);
            if (ticket.isExpired()) {
                memberTicket.expiredTickets.add(ticketDto);
                if(ticket instanceof StudyTicket)
                    memberTicket.studyTime += ticket.getTicketRecord().getActiveTime();
            } else {
                memberTicket.activeTicket = ticketDto;
            }
        }

        return memberTicket;
    }
}
