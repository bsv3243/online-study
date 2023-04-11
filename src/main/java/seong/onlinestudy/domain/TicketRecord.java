package seong.onlinestudy.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Getter
public class TicketRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_record_id")
    private Long id;

    private LocalDateTime expiredTime;
    private long activeTime;

    @OneToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    public static TicketRecord create() {

        return new TicketRecord();
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        ticket.setTicketRecord(this);
    }

    public static TicketRecord create(Ticket ticket) {
        TicketRecord record = new TicketRecord();

        ZoneOffset offset = ZoneOffset.of("+09:00");
        LocalDateTime expiredTime = LocalDateTime.now();
        LocalDateTime startTime = ticket.getStartTime();

        record.expiredTime = expiredTime;
        record.activeTime = expiredTime.toEpochSecond(offset) - startTime.toEpochSecond(offset);

        record.setTicket(ticket);

        return record;
    }
}
