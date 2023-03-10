package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.request.TicketUpdateRequest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Getter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private long activeTime;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    private boolean isExpired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public static Ticket createTicket(TicketCreateRequest request, Member member, Study study, Group group) {
        Ticket ticket = new Ticket();
        ticket.startTime = LocalDateTime.now();
        ticket.ticketStatus = request.getStatus();
        ticket.activeTime = 0;
        ticket.isExpired = false;

        member.getTickets().add(ticket);
        ticket.member = member;

        study.getTickets().add(ticket);
        ticket.study = study;

        group.getTickets().add(ticket);
        ticket.group = group;

        return ticket;
    }

    public void updateStatus(TicketUpdateRequest updateRequest) {
        ZoneOffset offset = ZoneOffset.of("+09:00");
        if(updateRequest.getStatus() == TicketStatus.END) {
            this.endTime = LocalDateTime.now();
            this.activeTime = this.endTime.toEpochSecond(offset) - this.startTime.toEpochSecond(offset);
            this.isExpired = true;
        }
    }
}
