package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.StudyCreateRequest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Study {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "study")
    List<Ticket> tickets = new ArrayList<>();

    public static Study createStudy(StudyCreateRequest createRequest) {
        Study study = new Study();
        study.name = createRequest.getName();

        return study;
    }
}
