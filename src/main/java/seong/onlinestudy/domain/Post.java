package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.PostCreateRequest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;
    private String title;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    List<PostStudy> postStudies = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    List<Comment> comments = new ArrayList<>();

    public void setGroup(Group group) {
        this.group = group;
        group.getPosts().add(this);
    }

    public static Post createPost(PostCreateRequest request, Member member) {
        Post post = new Post();
        post.title = request.getTitle();
        post.content = request.getContent();
        post.category = request.getCategory();

        post.member = member;
        member.getPosts().add(post);

        return post;
    }
}
