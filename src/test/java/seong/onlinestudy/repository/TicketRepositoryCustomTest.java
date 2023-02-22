package seong.onlinestudy.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.MemberCreateRequest;
import seong.onlinestudy.request.StudyCreateRequest;

import javax.persistence.EntityManager;

import static seong.onlinestudy.domain.QGroup.group;
import static seong.onlinestudy.domain.QTicket.ticket;

@DataJpaTest
public class TicketRepositoryCustomTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    TicketRepository ticketRepository;

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        Member member1 = createMember("tester1");
        Group group1 = createGroup("테스트그룹1", 30, member1);

        memberRepository.save(member1);
        groupRepository.save(group1);

        StudyCreateRequest studyCreateRequest = new StudyCreateRequest();
        studyCreateRequest.setName("테스트스터디");
        Study study = Study.createStudy(studyCreateRequest);

        studyRepository.save(study);

        Ticket ticket = Ticket.createTicket(member1, study, group1);

        ticketRepository.save(ticket);
    }



    private Group createGroup(String name, int headcount, Member member) {
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName(name);
        request.setHeadcount(headcount);

        GroupMember groupMember = GroupMember.createGroupMember(member, GroupRole.MASTER);

        return Group.createGroup(request, groupMember);
    }

    private Member createMember(String username) {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setUsername(username);
        request.setNickname(username);
        request.setPassword(username);

        return Member.createMember(request);
    }
}
