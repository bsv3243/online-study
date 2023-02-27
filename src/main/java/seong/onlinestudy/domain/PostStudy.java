package seong.onlinestudy.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class PostStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_study_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    public static PostStudy create(Post post, Study study) {
        PostStudy postStudy = new PostStudy();
        postStudy.post = post;
        post.getPostStudies().add(postStudy);

        postStudy.study = study;
        study.getPostStudies().add(postStudy);

        return postStudy;
    }
}
