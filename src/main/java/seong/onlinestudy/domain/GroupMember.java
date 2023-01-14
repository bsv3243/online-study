package seong.onlinestudy.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Table(name = "group_member")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    private LocalDate joinedAt;

    @Enumerated(EnumType.STRING)
    private GroupRole role;

    public void setGroup(Group group) {
        this.group = group;
    }

    public static GroupMember createGroupMember(Member member, GroupRole role) {
        GroupMember groupMember = new GroupMember();
        groupMember.member = member;
        groupMember.role = role;

        return groupMember;
    }
}