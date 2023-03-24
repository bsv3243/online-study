package seong.onlinestudy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.TicketGetRequest;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.request.TicketUpdateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.domain.TicketStatus.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    TicketService ticketService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    TicketRepository ticketRepository;

    @Mock
    StudyRepository studyRepository;

    List<Member> testMembers;
    List<Group> testGroups;
    List<Study> testStudies;
    List<Ticket> testStudyTickets;
    List<Ticket> testRestTickets;

    @BeforeEach
    void testDateInit() {
        testMembers = createMembers(10, true);
        testGroups = createGroups(testMembers, 2);
        testStudies = createStudies(2, true);
        testStudyTickets = createTickets(STUDY, testMembers, testGroups, testStudies);
        testRestTickets = createTickets(REST, testMembers, testGroups, testStudies);
    }

    @Test
    void createTicket() {
        //given
        Study study = MyUtils.createStudy("테스트스터디");
        Member member = MyUtils.createMember("member", "member");
        Group group = createGroup("그룹", 30, member);
        setField(study, "id", 1L);
        setField(member, "id", 1L);
        setField(group, "id", 1L);

        given(studyRepository.findById(any())).willReturn(Optional.of(study));
        given(memberRepository.findById(any())).willReturn(Optional.of(member));
        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(ticketRepository.findByMemberAndExpiredFalse(member)).willReturn(Optional.empty());

        //when
        TicketCreateRequest request = new TicketCreateRequest();
        request.setStatus(STUDY); request.setGroupId(group.getId()); request.setStudyId(study.getId());

        Long ticketId = ticketService.createTicket(request, member);

        //then
    }

    @Test
    void expiredTicket() throws InterruptedException {
        //given
        Study study = MyUtils.createStudy("테스트스터디");
        Member member = MyUtils.createMember("member", "member");
        Group group = createGroup("그룹", 30, member);
        Ticket ticket = MyUtils.createTicket(STUDY, member, study, group);
        setField(study, "id", 1L);
        setField(member, "id", 1L);
        setField(group, "id", 1L);
        setField(ticket, "id", 1L);


        given(ticketRepository.findById(1L)).willReturn(Optional.of(ticket));

        //when
        Thread.sleep(1000); // ticket.record 의 activeTime 을 조절하기 위해 1초동안 쓰레드를 중지한다.
        Long ticketId = ticketService.expireTicket(ticket.getId(), member);

        //then
        assertThat(ticket.isExpired()).isTrue();
        assertThat(ticket.getRecord().getExpiredTime()).isNotNull();
        assertThat(ticket.getRecord().getActiveTime()).isGreaterThan(0);
    }

    @Test
    @DisplayName("updateTicket_ex, 멤버 불일치")
    void expiredTicket_ex() {
        //given
        Member memberA = MyUtils.createMember("memberA", "memberA");
        Member memberB = MyUtils.createMember("memberB", "memberB");
        Study study = MyUtils.createStudy("테스트스터디");
        Group group = createGroup("그룹", 30, memberA);
        Ticket ticket = MyUtils.createTicket(STUDY, memberA, study, group);

        setField(memberA, "id", 1L);
        setField(memberB, "id", 2L);
        setField(study, "id", 1L);
        setField(group, "id", 1L);
        setField(ticket, "id", 1L);

        given(ticketRepository.findById(1L)).willReturn(Optional.of(ticket));

        //when

        //then
        assertThatThrownBy(() -> ticketService.expireTicket(ticket.getId(), memberB))
                .isInstanceOf(PermissionControlException.class);
    }

    @Test
    void getTickets_조건없음() {
        //given
        List<Ticket> tickets = new ArrayList<>();
        tickets.addAll(testStudyTickets);
        tickets.addAll(testRestTickets);

        expireTickets(tickets);

        given(ticketRepository.findTickets(any(), any(), any(), any(), any())).willReturn(tickets);

        //when
        TicketGetRequest request = new TicketGetRequest();
        Member loginMember = testMembers.get(0);

        List<MemberTicketDto> memberTickets = ticketService.getTickets(request, loginMember);

        //then
        List<Long> testMemberIds = testMembers.stream().map(Member::getId).collect(Collectors.toList());
        List<Long> findMemberIds = memberTickets.stream().map(MemberTicketDto::getMemberId).collect(Collectors.toList());

        assertThat(findMemberIds).containsExactlyInAnyOrderElementsOf(testMemberIds);
        assertThat(memberTickets).allSatisfy(memberTicket -> {
            List<Ticket> ticketsFrom = getTicketsOwnsByMember(tickets, memberTicket.getMemberId());
            List<TicketDto> targetTickets = ticketsFrom.stream().map(TicketDto::from).collect(Collectors.toList());

            assertThat(memberTicket.getActiveTicket()).isNull();
            assertThat(memberTicket.getExpiredTickets())
                    .containsExactlyInAnyOrderElementsOf(targetTickets);
        });
    }

    private List<Ticket> getTicketsOwnsByMember(List<Ticket> tickets, Long memberId) {
        return tickets.stream()
                .filter(ticket -> ticket.getMember().getId().equals(memberId))
                .collect(Collectors.toList());
    }
}