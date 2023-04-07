package seong.onlinestudy.domain;

import lombok.Getter;
import org.hibernate.annotations.Where;
import seong.onlinestudy.enumtype.PostCategory;
import seong.onlinestudy.request.post.PostCreateRequest;
import seong.onlinestudy.request.post.PostUpdateRequest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Where(clause = "deleted=false")
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
    private int viewCount;
    private LocalDateTime createdAt;
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "post", cascade = CascadeType.PERSIST)
    List<PostStudy> postStudies = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.PERSIST)
    List<Comment> comments = new ArrayList<>();

    public void setGroup(Group group) {
        this.group = group;
        group.getPosts().add(this);
    }

    /**
     * 조회수 증가
     */
    public void plusViewCount() {
        this.viewCount++;
    }

    public void update(PostUpdateRequest request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.category = request.getCategory();
    }

    public void delete() {
        this.deleted = true;
    }

    public static Post createPost(PostCreateRequest request, Member member) {
        Post post = new Post();
        post.title = request.getTitle();
        post.content = request.getContent();
        post.category = request.getCategory();
        post.viewCount = 0;
        post.createdAt = LocalDateTime.now();
        post.deleted = false;

        post.member = member;
        member.getPosts().add(post);

        return post;
    }
}
