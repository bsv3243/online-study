package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.TicketUpdateRequest;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    LocalDateTime startTime;
    LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    MemberStatus memberStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public static Ticket createTicket(Member member, Study study, Group group) {
        Ticket ticket = new Ticket();
        ticket.startTime = LocalDateTime.now();
        ticket.memberStatus = MemberStatus.STUDY;

        member.getTickets().add(ticket);
        ticket.member = member;

        study.tickets.add(ticket);
        ticket.study = study;

        group.getTickets().add(ticket);
        ticket.group = group;

        return ticket;
    }

    public void updateStatus(TicketUpdateRequest updateRequest) {
        this.memberStatus = updateRequest.getMemberStatus();
    }
}
