package seong.onlinestudy.domain;

import lombok.Getter;
import org.hibernate.annotations.Where;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.request.group.GroupCreateRequest;
import seong.onlinestudy.request.group.GroupUpdateRequest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Where(clause = "deleted = false")
@Table(name = "study_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;
    private String name;
    private int headcount;
    private LocalDateTime createdAt;
    private boolean deleted;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private GroupCategory category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> groupMembers = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Post> posts = new ArrayList<>();

    public void addGroupMember(GroupMember groupMember) {
        groupMembers.add(groupMember);
        groupMember.setGroup(this);
    }

    public void delete() {
        this.deleted = true;
    }

    public static Group createGroup(GroupCreateRequest createRequest, GroupMember groupMember) {
        Group group = new Group();
        group.name = createRequest.getName();
        group.category = createRequest.getCategory();
        group.headcount = createRequest.getHeadcount();
        group.createdAt = LocalDateTime.now();
        group.addGroupMember(groupMember);
        group.deleted = false;

        return group;
    }

    public void update(GroupUpdateRequest request) {
        if(request.getDescription() != null) {
            description = request.getDescription();
        }

        if(request.getHeadcount() != null) {
            if(this.groupMembers.size() > request.getHeadcount()) {
                throw new IllegalArgumentException("그룹 인원 제한은 현재 소속된 인원 수보다 작을 수 없습니다.");
            }
            headcount = request.getHeadcount();
        }
    }
}
