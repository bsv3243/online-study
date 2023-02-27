package seong.onlinestudy;

import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.request.PostCreateRequest;
import seong.onlinestudy.request.StudyCreateRequest;

import java.util.ArrayList;
import java.util.List;

public class MyUtils {

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

    public static Ticket createTicket(Member member, Study study, Group group) {
        return Ticket.createTicket(member, study, group);
    }

    public static Study createStudy(String name) {
        StudyCreateRequest request = new StudyCreateRequest();
        request.setName(name);

        return Study.createStudy(request);
    }

    public static List<Study> createStudies(int endId) {
        List<Study> studies = new ArrayList<>();
        for(int i=0; i<=endId; i++) {
            studies.add(createStudy("테스트스터디"+i));
        }
        return studies;
    }


    public static List<Member> createMembers(int endId) {
        List<Member> members = new ArrayList<>();
        for(int i=0; i<=endId; i++) {
            members.add(createMember("testMember" + i, "testMember" + i));
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

    public static List<Post> createPosts(List<Member> members, List<Group> groups, int endId) {
        List<Post> posts = new ArrayList<>();
        for(int i=0; i<=endId; i++) {
            Post post = createPost("testPost" + i, "testPost" + i, PostCategory.CHAT, members.get(i));
            post.setGroup(groups.get(i%groups.size()));
        }
        return posts;
    }
}
