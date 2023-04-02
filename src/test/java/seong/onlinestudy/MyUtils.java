package seong.onlinestudy;

import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupStudyDto;
import seong.onlinestudy.request.comment.CommentCreateRequest;
import seong.onlinestudy.request.group.GroupCreateRequest;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.request.post.PostCreateRequest;
import seong.onlinestudy.request.study.StudyCreateRequest;

import java.util.*;

import static org.springframework.test.util.ReflectionTestUtils.setField;
public class MyUtils {

    private static final Random random = new Random();

    public static Member createMember(String username, String password) {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername(username);
        request.setNickname(username);
        request.setPassword(password);

        return Member.createMember(request);
    }

    public static Group createGroup(String name, int headcount, Member member) {
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName(name);
        request.setHeadcount(headcount);

        GroupMember groupMember = GroupMember.createGroupMember(member, GroupRole.MASTER);

        return Group.createGroup(request, groupMember);
    }


    public static List<Group> createGroups(List<Member> members, int endId) {
        List<Group> groups = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            groups.add(createGroup("테스트그룹" + 1, 30, members.get(i)));
        }
        return groups;
    }

    public static List<Group> createGroups(List<Member> members, int endId, boolean setId) {
        List<Group> groups = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            groups.add(createGroup("테스트그룹" + 1, 30, members.get(i)));

            if(setId) {
                setField(groups.get(i), "id", (long) i);
            }

        }
        return groups;
    }

    public static Ticket createRestTicket(Member member, Group group) {
        return RestTicket.createRestTicket(member, group);
    }

    public static Ticket createStudyTicket(Member member, Group group, Study study) {
        return StudyTicket.createStudyTicket(member, group, study);
    }

    public static void setTicketEnd(Ticket ticket, long seconds) {
        setField(ticket.getRecord(), "activeTime", seconds);
        setField(ticket.getRecord(), "expiredTime", ticket.getStartTime().plusSeconds(seconds));
    }

    public static Study createStudy(String name) {
        StudyCreateRequest request = new StudyCreateRequest();
        request.setName(name);

        return Study.createStudy(request);
    }


    public static List<Study> createStudies(int endId) {
        List<Study> studies = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            studies.add(createStudy("테스트스터디"+1));
        }
        return studies;
    }

    public static List<Study> createStudies(int endId, boolean setId) {
        List<Study> studies = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            studies.add(createStudy("테스트스터디"+i));

            if(setId) {
                setField(studies.get(i), "id", (long) i);
            }
        }
        return studies;
    }


    public static GroupStudyDto createGroupStudyDto(Long studyId, Long groupId, String name, long studyTime) {
        GroupStudyDto groupStudyDto = new GroupStudyDto(studyId, groupId, name, studyTime);

        return groupStudyDto;
    }

    public static List<Member> createMembers(int endId) {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < endId; i++) {
            members.add(createMember("testMember" + i, "testMember" + i));
        }

        return members;
    }

    public static List<Member> createMembers(int beginId, int endId) {
        List<Member> members = new ArrayList<>();
        for (int i = beginId; i < endId; i++) {
            members.add(createMember("testMember" + i, "testMember" + i));
        }

        return members;
    }
            
    public static List<Study> createStudies(int beginId, int endId, boolean setId) {
        List<Study> studies = new ArrayList<>();
        for(int i=beginId; i<endId; i++) {
            studies.add(createStudy("테스트스터디"+i));

            if(setId) {
                setField(studies.get(i), "id", (long) i);
            }
        }
        return studies;
    }


    public static List<Member> createMembers(int endId, boolean setId) {
        List<Member> members = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            members.add(createMember("testMember" + i, "testMember" + i));

            if(setId) {
                setField(members.get(i), "id", (long) i);
            }
        }

        return members;
    }

    public static Post createPost(String title, String content, PostCategory category, Member member) {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setCategory(category);

        return Post.createPost(request, member);
    }

    public static List<PostStudy> createPostStudies(Post post, List<Study> studies) {
        List<PostStudy> postStudies = new ArrayList<>();
        for (Study study : studies) {
            postStudies.add(PostStudy.create(post, study));
        }

        return postStudies;
    }

    public static List<Post> createPosts(List<Member> members, List<Group> groups, int endId, boolean setId) {
        List<Post> posts = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            Post post = createPost("testPost" + i, "testPost" + i, PostCategory.CHAT, members.get(random.nextInt(members.size())));
            post.setGroup(groups.get(random.nextInt(groups.size())));
            posts.add(post);

            if(setId) {
                setField(post, "id", (long)i);
            }
        }
        return posts;
    }

    public static Comment createComment(String content) {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent(content);

        Comment comment = Comment.create(request);

        return comment;
    }

    public static List<Comment> createComments(List<Member> members, List<Post> posts, int endId, boolean setId) {
        List<Comment> comments = new ArrayList<>();
        for(int i=0; i<endId; i++) {
            Comment comment = createComment("테스트댓글" + i);
            comment.setMemberAndPost(members.get(i%members.size()), posts.get(i%posts.size()));

            if(setId) {
                setField(comment, "id", (long) i);
            }
            comments.add(comment);
        }
        return comments;
    }

    public static void joinMembersToGroups(List<Member> members, List<Group> groups) {
        for(int i=groups.size(); i<members.size(); i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i % groups.size()).addGroupMember(groupMember);
        }
    }

    public static List<Ticket> createStudyTickets(List<Member> members, List<Group> groups, List<Study> studies, boolean setId) {
        List<Ticket> tickets = new ArrayList<>();

        Map<Member, Ticket> memberActiveTicket = new HashMap<>();
        for(int i=0; i<members.size()*4; i++) {
            Member targetMember = members.get(random.nextInt(members.size()));

            Ticket ticket = createStudyTicket(
                    targetMember,
                    groups.get(random.nextInt(groups.size())),
                    studies.get(random.nextInt(studies.size()))
            );

            if(setId) {
                setField(ticket, "id", (long) i);
            }

            tickets.add(ticket);

            //회원의 활성 상태 티켓이 존재한다면 만료시킨다.
            if(memberActiveTicket.containsKey(targetMember)) {
                Ticket activeTicket = memberActiveTicket.get(targetMember);
                activeTicket.expiredAndUpdateRecord();
            }
            memberActiveTicket.put(targetMember, ticket);
        }
        return tickets;
    }

    public static List<Ticket> createRestTickets(List<Member> members, List<Group> groups, boolean setId) {
        List<Ticket> tickets = new ArrayList<>();
        for(int i=0; i<members.size(); i++) {
            Ticket ticket = createRestTicket(
                    members.get(random.nextInt(members.size())),
                    groups.get(random.nextInt(groups.size()))
            );
            tickets.add(ticket);
            if(setId) {
                setField(ticket, "id", (long) i);
            }
        }
        return tickets;
    }

    public static List<Ticket> createStudyTickets(TicketStatus status, List<Member> members, List<Group> groups,
                                                  List<Study> studies, boolean setId) {
        List<Ticket> tickets = new ArrayList<>();
        for(int i=0; i<members.size(); i++) {
            Ticket ticket = createStudyTicket(members.get(i), groups.get(i % groups.size()), studies.get(i % studies.size()));
            tickets.add(ticket);
            setField(ticket, "id", (long)i);
        }
        return tickets;
    }

    public static List<Ticket> createEndTickets(List<Member> members, List<Group> groups, List<Study> studies, long studyTime) {
        List<Ticket> tickets = new ArrayList<>();
        for(int i=0; i<members.size(); i++) {
            Ticket ticket = createStudyTicket(members.get(i), groups.get(i % groups.size()), studies.get(i % studies.size()));
            tickets.add(ticket);
        }
        for (Ticket ticket : tickets) {
            setField(ticket, "expired", true);
            setField(ticket.getRecord(), "expiredTime", ticket.getStartTime().plusSeconds(studyTime));
            setField(ticket.getRecord(), "activeTime", studyTime);
        }
        return tickets;
    }


    public static void expireTickets(List<Ticket> tickets) {
        for (Ticket ticket : tickets) {
            ticket.expiredAndUpdateRecord();
        }
    }

    public static void expireTicket(Ticket ticket, int studyTime) {
        ticket.expiredAndUpdateRecord();
        setField(ticket.getRecord(), "activeTime", studyTime);
        setField(ticket.getRecord(), "expiredTime", ticket.getStartTime().plusSeconds(studyTime));
    }
}
