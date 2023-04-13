package seong.onlinestudy.domain;

import lombok.Getter;
import org.hibernate.annotations.Where;
import org.springframework.util.StringUtils;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.request.member.MemberUpdateRequest;

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
    private boolean deleted;

    @OneToMany(mappedBy = "member")
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
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void delete() {
        this.deleted = true;
    }

    public static Member createMember(MemberCreateRequest request) {
        Member member = new Member();
        member.username = request.getUsername();
        member.password = request.getPassword();
        member.nickname = request.getNickname();
        member.createdAt = LocalDateTime.now();
        member.deleted = false;

        return member;
    }
}
