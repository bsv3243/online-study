package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.TicketGetRequest;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.exception.DuplicateElementException;
import seong.onlinestudy.exception.UnAuthorizationException;
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.request.TicketUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static seong.onlinestudy.domain.TicketStatus.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final StudyRepository studyRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createTicket(TicketCreateRequest request, Member loginMember) {
        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new NoSuchElementException("잘못된 접근입니다."));

        Study findStudy = studyRepository.findById(request.getStudyId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 스터디입니다."));

        Group findGroup = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        ticketRepository.findByMemberAndTicketStatusIn(member, List.of(STUDY, REST))
                .ifPresent(ticket -> {
                    throw new DuplicateElementException("이전에 발급받은 티켓이 존재합니다.");
                });

        if (request.getStatus().equals(END)) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        Ticket ticket = Ticket.createTicket(request, member, findStudy, findGroup);
        ticketRepository.save(ticket);

        return ticket.getId();
    }

    @Transactional
    public Long updateTicket(Long ticketId, TicketUpdateRequest updateTicketRequest, Member loginMember) {
        Ticket findTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        //티켓의 회원 ID와 일치하지 않으면
        if(!findTicket.getMember().getId().equals(loginMember.getId())) {
            throw new UnAuthorizationException("권한이 없습니다.");
        }

        findTicket.updateStatus(updateTicketRequest);

        return findTicket.getId();
    }

    public TicketDto getTicket(Long ticketId) {
        Ticket findTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        TicketDto ticketDto = TicketDto.from(findTicket);

        return ticketDto;
    }

    public List<MemberTicketDto> getTickets(TicketGetRequest request) {
        //하루의 시작을 05시로 한다.
        LocalDateTime startTime = request.getDate().atStartOfDay().plusHours(5);

        List<Member> membersWithTickets
                = ticketRepository.findMembersWithTickets(startTime, startTime.plusDays(request.getDays()), request.getGroupId());

        return membersWithTickets.stream().map(MemberTicketDto::from).collect(Collectors.toList());
    }
}
