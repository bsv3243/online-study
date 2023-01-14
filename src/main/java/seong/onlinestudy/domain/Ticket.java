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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    Room room;

    public static Ticket createTicket(Study study, Room room, Member member) {
        Ticket ticket = new Ticket();
        ticket.startTime = LocalDateTime.now();
        ticket.memberStatus = MemberStatus.STUDY;
        ticket.member = member;
        ticket.study = study;
        ticket.room = room;

        return ticket;
    }

    public void updateStatus(TicketUpdateRequest updateRequest) {
        this.memberStatus = updateRequest.getMemberStatus();
    }
}
