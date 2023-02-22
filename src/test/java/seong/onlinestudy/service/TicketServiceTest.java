package seong.onlinestudy.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.exception.InvalidAuthorizationException;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.request.TicketUpdateRequest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static seong.onlinestudy.domain.MemberStatus.REST;

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
        updateRequest.setMemberStatus(REST);

        given(ticketRepository.findById(1L)).willReturn(Optional.of(testTicket));

        //when
        Long ticketId = ticketService.updateTicket(testTicket.getId(), updateRequest, member);

        //then
        assertThat(testTicket.getMemberStatus()).isEqualTo(REST);
    }

    @Test
    @DisplayName("updateTicket_ex, 멤버 불일치")
    void updateTicket_ex() {
        //given
        Member memberA = createMember(1L);
        Member memberB = createMember(2L);
        Ticket testTicket = getTestTicket(memberA);

        TicketUpdateRequest updateRequest = new TicketUpdateRequest();
        updateRequest.setMemberStatus(REST);

        given(ticketRepository.findById(1L)).willReturn(Optional.of(testTicket));

        //when

        //then
        assertThatThrownBy(() -> ticketService.updateTicket(testTicket.getId(), updateRequest, memberB))
                .isInstanceOf(InvalidAuthorizationException.class);
    }

    private TicketCreateRequest createTicketRequest(Long studyId) {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setStudyId(studyId);
        return request;
    }

    private Ticket getTestTicket(Member member) {
        Ticket ticket = new Ticket();

        ReflectionTestUtils.setField(ticket, "id", 1L);
        ReflectionTestUtils.setField(ticket, "startTime", LocalDateTime.now());
        ReflectionTestUtils.setField(ticket, "memberStatus", MemberStatus.STUDY);
        ReflectionTestUtils.setField(ticket, "member", member);

        return ticket;
    }


    private Study createStudy() {
        Study study = new Study();

        ReflectionTestUtils.setField(study, "id", 1L);
        ReflectionTestUtils.setField(study, "name", "테스트");

        return study;
    }

    private Member createMember(Long memberId) {
        Member member = new Member();

        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "username", "test1234");
        ReflectionTestUtils.setField(member, "password", "test1234");

        return member;
    }
}