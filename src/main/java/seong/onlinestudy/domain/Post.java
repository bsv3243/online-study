package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.PostCreateRequest;
import seong.onlinestudy.request.PostUpdateRequest;

import javax.persistence.*;
import java.time.LocalDateTime;
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
    private int viewCount;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

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

    /**
     * 조회수 증가
     */
    public void plusViewCount() {
        this.viewCount++;
    }

    /**
     * 제목, 본문 업데이트
     * @param request title(제목), content(본문)
     */
    public void update(PostUpdateRequest request) {
        this.title = request.getTitle();
        this.content = request.getContent();
    }

    /**
     * 게시글 삭제 상태로 변경,
     * postStudies 리스트 clear
     */
    public void delete() {
        this.isDeleted = true;
        this.postStudies.clear();
    }

    public static Post createPost(PostCreateRequest request, Member member) {
        Post post = new Post();
        post.title = request.getTitle();
        post.content = request.getContent();
        post.category = request.getCategory();
        post.viewCount = 0;
        post.createdAt = LocalDateTime.now();
        post.isDeleted = false;

        post.member = member;
        member.getPosts().add(post);

        return post;
    }
}
