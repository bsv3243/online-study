package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.MemberCreateRequest;

import javax.persistence.*;

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

    public static Member createMember(MemberCreateRequest request) {
        Member member = new Member();
        member.username = request.getUsername();
        member.password = request.getPassword();
        member.nickname = request.getNickname();

        return member;
    }
}
