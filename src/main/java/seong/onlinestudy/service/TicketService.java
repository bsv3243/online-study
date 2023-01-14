package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.domain.Room;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.exception.InvalidAuthorizationException;
import seong.onlinestudy.repository.RoomRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.request.TicketUpdateRequest;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final StudyRepository studyRepository;
    private final RoomRepository roomRepository;

    public Long createTicket(TicketCreateRequest createTicketRequest, Member loginMember) {
        Study findStudy = studyRepository.findById(createTicketRequest.getStudyId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 스터디입니다."));

        Room findRoom = roomRepository.findById(createTicketRequest.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 방입니다."));

        Ticket ticket = Ticket.createTicket(findStudy, findRoom, loginMember);
        ticketRepository.save(ticket);

        return ticket.getId();
    }

    public Long updateTicket(Long ticketId, TicketUpdateRequest updateTicketRequest, Member loginMember) {
        Ticket findTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        //티켓의 회원 ID와 일치하지 않으면
        if(!findTicket.getMember().getId().equals(loginMember.getId())) {
            throw new InvalidAuthorizationException("권한이 없습니다.");
        }

        findTicket.updateStatus(updateTicketRequest);

        return findTicket.getId();
    }
}
