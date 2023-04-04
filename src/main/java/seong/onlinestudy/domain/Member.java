package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.member.MemberCreateRequest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<GroupMember> groupMembers = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Ticket> tickets = new ArrayList<>();

    public void update(MemberUpdateRequest request) {
        if (StringUtils.hasText(request.getNickname())) {
            this.nickname = request.getNickname();
        }
        if (StringUtils.hasText(request.getPassword())) {
            this.password = request.getPassword();
        }
    }

    public static Member createMember(MemberCreateRequest request) {
        Member member = new Member();
        member.username = request.getUsername();
        member.password = request.getPassword();
        member.nickname = request.getNickname();

        return member;
    }
}
