package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.constant.TimeConst;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ticket_status")
public abstract class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    private LocalDateTime startTime;

    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToOne(mappedBy = "ticket", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private TicketRecord ticketRecord;

    protected Ticket() {
    }

    protected Ticket(Member member, Group group) {
        this.startTime = LocalDateTime.now();
        this.setMember(member);
        this.setGroup(group);
    }

    public void setMember(Member member) {
        this.member = member;
        member.getTickets().add(this);
    }

    public void setGroup(Group group) {
        this.group = group;
        group.getTickets().add(this);
    }

    public void setTicketRecord(TicketRecord ticketRecord) {
        this.ticketRecord = ticketRecord;
    }

    public LocalDate getDateBySetting() {
        if(startTime.getHour() < TimeConst.DAY_START) {
            return startTime.toLocalDate().minusDays(1);
        } else {
            return startTime.toLocalDate();
        }
    }

    public void expireAndCreateRecord() {
        expired = true;

        TicketRecord.create(this);
    }
}
