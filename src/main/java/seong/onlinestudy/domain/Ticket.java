package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.TimeConst;
import seong.onlinestudy.request.TicketCreateRequest;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public LocalDate getDateBySetting() {
        if(startTime.getHour() < TimeConst.DAY_START) {
            return startTime.toLocalDate().minusDays(1);
        } else {
            return startTime.toLocalDate();
        }
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "record_id")
    private Record record;

    public void setRecord(Record record) {
        this.record = record;
    }

    public void expiredAndUpdateRecord() {
        expired = true;
        record.update(this);
    }

    public static Ticket createWithRecord(TicketCreateRequest request, Member member, Study study, Group group) {
        Ticket ticket = new Ticket();
        ticket.startTime = LocalDateTime.now();
        ticket.ticketStatus = request.getStatus();
        ticket.expired = false;

        member.getTickets().add(ticket);
        ticket.member = member;

        if(request.getStatus() == TicketStatus.STUDY) {
            study.getTickets().add(ticket);
            ticket.study = study;
        }

        group.getTickets().add(ticket);
        ticket.group = group;

        Record.create(ticket);

        return ticket;
    }
}
