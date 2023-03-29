package seong.onlinestudy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.ticket.TicketGetRequest;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.ticket.TicketCreateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
        testGroups = createGroups(testMembers, 2, true);
        joinMembersToGroups(testMembers, testGroups);

        testStudies = createStudies(2, true);
        testStudyTickets = createStudyTickets(testMembers, testGroups, testStudies, true);
        testRestTickets = createRestTickets(testMembers, testGroups, true);
    }

    @Test
    @DisplayName("티켓 생성")
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
    @DisplayName("티켓 만료")
    void expiredTicket() throws InterruptedException {
        //given
        Study study = MyUtils.createStudy("테스트스터디");
        Member member = MyUtils.createMember("member", "member");
        Group group = createGroup("그룹", 30, member);
        Ticket ticket = MyUtils.createStudyTicket(member, group, study);
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
    @DisplayName("티켓 만료_예외, 멤버 불일치")
    void expiredTicket_ex() {
        //given
        Member memberA = MyUtils.createMember("memberA", "memberA");
        Member memberB = MyUtils.createMember("memberB", "memberB");
        Study study = MyUtils.createStudy("테스트스터디");
        Group group = createGroup("그룹", 30, memberA);
        Ticket ticket = MyUtils.createStudyTicket(memberA, group, study);

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
    @DisplayName("티켓 목록 조회_ticket 데이터 없음, 조건 없음")
    public void getTickets_NoDataWithoutCondition() {
        //given
        PageImpl<Member> membersPage = new PageImpl<>(testMembers);
        given(memberRepository.findMembersOrderByStudyTime(any(), any(), any())).willReturn(membersPage);

        //when
        TicketGetRequest request = new TicketGetRequest();
        List<MemberTicketDto> memberTickets = ticketService.getTickets(request);

        //then
        List<Long> findMemberIds = memberTickets.stream()
                .map(MemberTicketDto::getMemberId).collect(Collectors.toList());
        List<Long> testMemberIds = testMembers.stream()
                .map(Member::getId).collect(Collectors.toList());

        assertThat(findMemberIds).containsExactlyInAnyOrderElementsOf(testMemberIds);
        assertThat(memberTickets).allSatisfy(memberTicket -> {
            assertThat(memberTicket.getActiveTicket()).isNull();
            assertThat(memberTicket.getExpiredTickets()).isEmpty();
            assertThat(memberTicket.getStudyTime()).isZero();
        });

    }

    @Test
    @DisplayName("티켓 목록 조회_ticket 데이터 없음, memberId 조건 추가")
    void getTickets_NoDataWithCondition() {
        //given
        Member testMember = testMembers.get(0);
        List<Ticket> tickets = new ArrayList<>();

        given(ticketRepository.findTickets(anyList(), any(), any(), any(), any())).willReturn(tickets);
        given(memberRepository.findById(any())).willReturn(Optional.of(testMember));

        //when
        TicketGetRequest request = new TicketGetRequest();
        request.setMemberId(testMember.getId());

        List<MemberTicketDto> memberTickets = ticketService.getTickets(request);

        //then
        List<Long> findMemberIds = memberTickets.stream().map(MemberTicketDto::getMemberId).collect(Collectors.toList());

        assertThat(findMemberIds).containsExactlyInAnyOrderElementsOf(List.of(testMember.getId()));
        assertThat(memberTickets).allSatisfy(memberTicket -> {

            assertThat(memberTicket.getActiveTicket()).isNull();
            assertThat(memberTicket.getExpiredTickets()).isEmpty();
        });
    }

    @Test
    @DisplayName("티켓 목록 조회_ticket 데이터 존재, 아무런 조건도 주어지지 않음")
    void getTickets_DataWithoutCondition() {
        //given
        List<Ticket> tickets = new ArrayList<>();
        tickets.addAll(testStudyTickets);
        tickets.addAll(testRestTickets);

        expireTickets(tickets);

        PageRequest pageRequest = PageRequest.of(0, testMembers.size());
        PageImpl<Member> testMembersPage = new PageImpl<>(testMembers);

        given(memberRepository.findMembersOrderByStudyTime(any(), any(), any())).willReturn(testMembersPage);
        given(ticketRepository.findTickets(anyList(), any(), any(), any(), any())).willReturn(tickets);

        //when
        TicketGetRequest request = new TicketGetRequest();
        Member loginMember = testMembers.get(0);

        List<MemberTicketDto> memberTickets = ticketService.getTickets(request);

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

    @Test
    @DisplayName("티켓 목록 조회_ticket 데이터 존재, groupId 조건")
    void getTickets_DataWithCondition() {
        //given
        Group testGroup = testGroups.get(0);
        List<Member> testMembersInGroup = testGroup.getGroupMembers().stream()
                .map(GroupMember::getMember).collect(Collectors.toList());
        List<Ticket> testTicketsInGroup = testGroup.getTickets();

        //활성화된 티켓을 testRestTickets 로 한정시키기 위함
        expireTickets(testStudyTickets);

        List<Ticket> testActiveTickets = new ArrayList<>();
        for(int i=0; i<testMembersInGroup.size(); i++) {
            Member testMember = testMembersInGroup.get(i);
            Study testStudy = testStudies.get(i % testStudies.size());
            Ticket studyTicket = StudyTicket.createStudyTicket(testMember, testGroup, testStudy);

            testActiveTickets.add(studyTicket);
        }

        given(groupRepository.findGroupWithMembers(any())).willReturn(Optional.of(testGroup));
        given(ticketRepository.findTickets(anyList(), any(), any(), any(), any())).willReturn(testTicketsInGroup);

        //when
        TicketGetRequest request = new TicketGetRequest();
        request.setGroupId(testGroup.getId());
        List<MemberTicketDto> memberTickets = ticketService.getTickets(request);

        //then
        List<Long> findMemberIds = memberTickets.stream()
                .map(MemberTicketDto::getMemberId).collect(Collectors.toList());
        List<Long> testMemberIds = testMembersInGroup.stream()
                .map(Member::getId).collect(Collectors.toList());

        List<Long> findActiveTicketIds = memberTickets.stream()
                .map(memberTicketDto -> memberTicketDto.getActiveTicket().getTicketId())
                .collect(Collectors.toList());

        List<Long> testActiveTicketIds = testActiveTickets.stream().map(Ticket::getId).collect(Collectors.toList());

        assertThat(findMemberIds).isEqualTo(testMemberIds);
        assertThat(findActiveTicketIds).containsExactlyInAnyOrderElementsOf(testActiveTicketIds);

    }

    private List<Ticket> getTicketsOwnsByMember(List<Ticket> tickets, Long memberId) {
        return tickets.stream()
                .filter(ticket -> ticket.getMember().getId().equals(memberId))
                .collect(Collectors.toList());
    }
}