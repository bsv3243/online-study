package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.enumtype.TicketStatus;

import javax.persistence.*;

@Entity
@Getter
@DiscriminatorValue(TicketStatus.Values.STUDY)
public class StudyTicket extends Ticket{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "study_id")
    private Study study;

    protected StudyTicket() {
    }

    private StudyTicket(Member member, Group group) {
        super(member, group);
    }

    public void setStudy(Study study) {
        this.study = study;
        study.getStudyTickets().add(this);
    }

    public static Ticket createStudyTicket(Member member, Group group, Study study) {
        StudyTicket studyTicket = new StudyTicket(member, group);

        studyTicket.setStudy(study);

        return studyTicket;
    }
}
