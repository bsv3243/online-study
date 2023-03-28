package seong.onlinestudy.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Getter
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    private LocalDateTime expiredTime;
    private long activeTime;

    public static Record create() {

        return new Record();
    }

    public void update(Ticket ticket) {
        LocalDateTime expiredTime = LocalDateTime.now();
        ZoneOffset offset = ZoneOffset.of("+09:00");

        this.expiredTime = expiredTime;
        this.activeTime = expiredTime.toEpochSecond(offset) - ticket.getStartTime().toEpochSecond(offset);
    }
}
