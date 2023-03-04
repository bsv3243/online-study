package seong.onlinestudy.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.request.TicketUpdateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.domain.TicketStatus.END;
import static seong.onlinestudy.domain.TicketStatus.STUDY;

@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    StudyRepository studyRepository;


    @BeforeEach
    void init() {
        List<Member> members = createMembers(50);
        memberRepository.saveAll(members);

        List<Group> groups = createGroups(members, 2);
        groupRepository.saveAll(groups);

        for(int i=2; i<50; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i % groups.size()).addGroupMember(groupMember);
        }

        Study study = createStudy("study");
        studyRepository.save(study);

        List<Ticket> tickets = new ArrayList<>();
        for(int i=0; i<50; i++) {
            tickets.add(createTicket(STUDY, members.get(i), study, groups.get(i % groups.size())));
        }
        ticketRepository.saveAll(tickets);
        for(int i=0; i<20; i++) {
            TicketUpdateRequest request = new TicketUpdateRequest();

            request.setTicketStatus(END);
            tickets.get(i).updateStatus(request);
        }
    }

    @Test
    void findMembersWithTickets() {
        //given
        List<Member> members = createMembers(50, 70);
        memberRepository.saveAll(members);

        Group group = createGroup("groupA", 30, members.get(0));
        groupRepository.save(group);

        for(int i=1; i<20; i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            group.addGroupMember(groupMember);
        }

        Study study = createStudy("studyA");
        studyRepository.save(study);

        List<Ticket> tickets = new ArrayList<>();
        for (Member member : members) {
            tickets.add(createTicket(STUDY, member, study, group));
        }
        ticketRepository.saveAll(tickets);

        //when
        List<Member> findMembers = ticketRepository.findMembersWithTickets(LocalDate.now().atStartOfDay(),
                LocalDate.now().atStartOfDay().plusDays(1), group.getId());
//        List<Member> findMembers = ticketRepository.findMembersWithTickets(group.getId());

        //then
        List<String> usernames = members.stream().map(Member::getUsername).collect(Collectors.toList());
        List<String> findUsernames = findMembers.stream().map(Member::getUsername).collect(Collectors.toList());

        assertThat(usernames).containsExactlyInAnyOrderElementsOf(findUsernames);

        List<Ticket> findTickets = new ArrayList<>();
        for (Member member : members) {
            findTickets.addAll(member.getTickets());
        }
        assertThat(findTickets).containsExactlyInAnyOrderElementsOf(tickets);
        assertThat(findTickets.size()).isEqualTo(tickets.size());
    }
}