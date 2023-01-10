package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.domain.role.GroupRole;

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

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private LocalDate joinedAt;
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
