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

    public static TicketRecord create() {

        return new TicketRecord();
    }

    public void update(Ticket ticket) {
        LocalDateTime expiredTime = LocalDateTime.now();
        ZoneOffset offset = ZoneOffset.of("+09:00");

        this.expiredTime = expiredTime;
        this.activeTime = expiredTime.toEpochSecond(offset) - ticket.getStartTime().toEpochSecond(offset);
    }
}
