package seong.onlinestudy.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seong.onlinestudy.MyUtils;
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
import static seong.onlinestudy.MyUtils.createGroup;
import static seong.onlinestudy.MyUtils.createMembers;
import static seong.onlinestudy.domain.TicketStatus.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    TicketService ticketService;


    @Mock
    TicketRepository ticketRepository;

    @Mock
    StudyRepository studyRepository;

    @Test
    void createTicket() {
        //given
        Study study = createStudy();
        Member member = createMember(1L);

        given(studyRepository.findById(any())).willReturn(Optional.of(study));

        TicketCreateRequest ticketRequest = createTicketRequest(study.getId());

        //when
        Long ticketId = ticketService.createTicket(ticketRequest, member);

        //then
    }

    @Test
    void updateTicket() {
        //given
        Member member = createMember(1L);
        Ticket testTicket = getTestTicket(member);

        TicketUpdateRequest updateRequest = new TicketUpdateRequest();
        updateRequest.setTicketStatus(REST);

        given(ticketRepository.findById(1L)).willReturn(Optional.of(testTicket));

        //when
        Long ticketId = ticketService.updateTicket(testTicket.getId(), updateRequest, member);

        //then
        assertThat(testTicket.getTicketStatus()).isEqualTo(REST);
    }

    @Test
    @DisplayName("updateTicket_ex, 멤버 불일치")
    void updateTicket_ex() {
        //given
        Member memberA = createMember(1L);
        Member memberB = createMember(2L);
        Ticket testTicket = getTestTicket(memberA);

        TicketUpdateRequest updateRequest = new TicketUpdateRequest();
        updateRequest.setTicketStatus(REST);

        given(ticketRepository.findById(1L)).willReturn(Optional.of(testTicket));

        //when

        //then
        assertThatThrownBy(() -> ticketService.updateTicket(testTicket.getId(), updateRequest, memberB))
                .isInstanceOf(PermissionControlException.class);
    }

    @Test
    void getTickets() {
        //given
        Long groupId = 1L;
        LocalDate startTime = LocalDate.now();

        TicketGetRequest request = new TicketGetRequest();
        request.setGroupId(groupId);
        request.setDate(startTime);
        request.setDays(1);

        List<Member> members = createMembers(20, true);
        Group group = createGroup("테스트그룹", 30, members.get(0));
        setField(group, "id", 1L);

        Study study = MyUtils.createStudy("study");
        setField(study, "id", 1L);

        List<Ticket> activeTickets = new ArrayList<>();
        List<Ticket> expiredTickets = new ArrayList<>();
        long count = 1L;
        ZoneOffset offset = ZoneOffset.of("+09:00");
        for (Member member : members) {
            if(count > 1L) {
                GroupMember.createGroupMember(member, GroupRole.USER);
            }

            Ticket ticket = MyUtils.createTicket(END, member, study, group);
            setField(ticket, "id", count++);
            setField(ticket, "startTime", ticket.getStartTime().minusHours(2));
            setField(ticket, "endTime", ticket.getStartTime().plusHours(1));
            setField(ticket, "activeTime",
                    ticket.getEndTime().toEpochSecond(offset) - ticket.getStartTime().toEpochSecond(offset));

            expiredTickets.add(ticket);
        }
        for (Member member : members) {
            Ticket ticket = MyUtils.createTicket(STUDY, member, study, group);
            setField(ticket, "id", count++);
            activeTickets.add(ticket);
        }

//        given(ticketRepository.findMembersWithTickets(any(), any(), any(Long.class))).willReturn(members);

        //when
        List<MemberTicketDto> memberTickets = ticketService.getTickets(request, null);

        //then
        assertThat(memberTickets.size()).isEqualTo(members.size());
        List<TicketDto> findActiveTickets = memberTickets.stream()
                .map(MemberTicketDto::getActiveTicket).collect(Collectors.toList());
        List<TicketDto> findExpiredTickets = new ArrayList<>();
        for (MemberTicketDto memberTicket : memberTickets) {
            findExpiredTickets.addAll(memberTicket.getExpiredTickets());
        }

        assertThat(findActiveTickets).allSatisfy(ticket -> {
            assertThat(ticket.getStatus()).isNotEqualTo(END);
            assertThat(ticket.getEndTime()).isNull();
        });
        assertThat(findExpiredTickets).allSatisfy(ticket -> {
            assertThat(ticket.getStatus()).isEqualTo(END);
            assertThat(ticket.getEndTime()).isNotNull();
        });
    }

    private TicketCreateRequest createTicketRequest(Long studyId) {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setStudyId(studyId);
        return request;
    }

    private Ticket getTestTicket(Member member) {
        Ticket ticket = new Ticket();

        setField(ticket, "id", 1L);
        setField(ticket, "startTime", LocalDateTime.now());
        setField(ticket, "memberStatus", TicketStatus.STUDY);
        setField(ticket, "member", member);

        return ticket;
    }


    private Study createStudy() {
        Study study = new Study();

        setField(study, "id", 1L);
        setField(study, "name", "테스트");

        return study;
    }

    private Member createMember(Long memberId) {
        Member member = new Member();

        setField(member, "id", memberId);
        setField(member, "username", "test1234");
        setField(member, "password", "test1234");

        return member;
    }
}