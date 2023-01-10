package seong.onlinestudy.domain;

import lombok.Getter;
import seong.onlinestudy.request.GroupCreateRequest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;
    private String name;
    private int headcount;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> groupMembers = new ArrayList<>();

    public void addGroupMember(GroupMember groupMember) {
        groupMembers.add(groupMember);
        groupMember.setGroup(this);
    }

    public static Group createGroup(GroupCreateRequest createRequest, GroupMember groupMember) {
        Group group = new Group();
        group.name = createRequest.getName();
        group.headcount = createRequest.getHeadcount();
        group.addGroupMember(groupMember);

        return group;
    }
}
