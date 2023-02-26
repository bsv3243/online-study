package seong.onlinestudy;

import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.GroupStudyDto;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;
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
        for(int i=0; i<endId; i++) {
            studies.add(createStudy("테스트스터디"+1));
        }
        return studies;
    }

    public static GroupStudyDto createGroupStudyDto(Long studyId, Long groupId, String name, long studyTime) {
        GroupStudyDto groupStudyDto = new GroupStudyDto(studyId, groupId, name, studyTime);

        return groupStudyDto;
    }

    public static List<Member> createMembers(int endId) {
        List<Member> members = new ArrayList<>();
        for(int i=0; i<=endId; i++) {
            members.add(createMember("testMember" + i, "testMember" + i));
        }

        return members;
    }
}
