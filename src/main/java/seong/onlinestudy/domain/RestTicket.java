package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.enumtype.TicketStatus;

import javax.persistence.*;

@Entity
@Getter
@DiscriminatorValue(TicketStatus.Values.REST)
@PrimaryKeyJoinColumn(name = "rest_ticket_id")
public class RestTicket extends Ticket {

    protected RestTicket() {
    }

    private RestTicket(Member member, Group group) {
        super(member, group);
    }

    public static Ticket createRestTicket(Member member, Group group) {
        RestTicket restTicket = new RestTicket(member, group);

        return restTicket;
    }
}
