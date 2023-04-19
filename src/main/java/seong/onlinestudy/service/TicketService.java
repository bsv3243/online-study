package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.constant.TimeConst;
import seong.onlinestudy.repository.*;
import seong.onlinestudy.request.ticket.TicketGetRequest;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.MemberTicketDto;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.exception.DuplicateElementException;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.request.ticket.TicketCreateRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static seong.onlinestudy.enumtype.TicketStatus.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final StudyRepository studyRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final TicketRecordRepository ticketRecordRepository;

    @Transactional
    public Long createTicket(TicketCreateRequest request, Long loginMemberId) {
        Member findMember = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new NoSuchElementException("잘못된 접근입니다."));

        Group findGroup = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        ticketRepository.findByMemberAndExpiredFalse(findMember)
                .ifPresent(ticket -> {
                    throw new DuplicateElementException("이전에 발급받은 티켓이 존재합니다.");
                });

        Ticket ticket;
        if(request.getStatus().equals(STUDY)) {
            Study findStudy = studyRepository.findById(request.getStudyId())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 스터디입니다."));

            ticket = StudyTicket.createStudyTicket(findMember, findGroup, findStudy);
        } else {
            ticket = RestTicket.createRestTicket(findMember, findGroup);
        }
        ticketRepository.save(ticket);

        return ticket.getId();
    }

    @Transactional
    public Long expireTicket(Long ticketId, Long loginMemberId) {
        Ticket findTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        //티켓의 회원 ID와 일치하지 않으면
        if(!findTicket.getMember().getId().equals(loginMemberId)) {
            throw new PermissionControlException("권한이 없습니다.");
        }

        findTicket.expireAndCreateRecord();

        return findTicket.getId();
    }

    public TicketDto getTicket(Long ticketId) {
        Ticket findTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 티켓입니다."));

        TicketDto ticketDto = TicketDto.from(findTicket);

        return ticketDto;
    }

    public List<MemberTicketDto> getTickets(TicketGetRequest request) {
        //하루의 시작을 DAY_START 시로 한다.
        LocalDateTime startTime = request.getDate().atStartOfDay().plusHours(TimeConst.DAY_START);
        LocalDateTime endTime = startTime.plusDays(request.getDays());

        List<Member> findMembers = getMemberIdsByRequest(request);
        List<Long> findMemberIds = findMembers.stream().map(Member::getId).collect(Collectors.toList());

        List<Ticket> findTickets = ticketRepository
                .findTickets(findMemberIds, request.getGroupId(), request.getStudyId(),
                        startTime, endTime);

        return joinMembersAndTickets(findMembers, findTickets);
    }

    private List<Member> getMemberIdsByRequest(TicketGetRequest request) {
        LocalDateTime startTime = request.getDate().atStartOfDay().plusHours(TimeConst.DAY_START);
        LocalDateTime endTime = startTime.plusDays(request.getDays());

        List<Member> members;
        if(request.getGroupId() != null) {
            Group findGroup = groupRepository.findGroupWithMembers(request.getGroupId())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

            members = findGroup.getGroupMembers().stream()
                    .map(GroupMember::getMember).collect(Collectors.toList());
        }
        else if(request.getMemberId() != null) {
            Member findMember = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

            members = List.of(findMember);
        }
        else {
            PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
            members = memberRepository.findMembersOrderByStudyTime(startTime, endTime, pageRequest).getContent();
        }
        return members;
    }

    private List<MemberTicketDto> joinMembersAndTickets(List<Member> members, List<Ticket> tickets) {
        List<MemberTicketDto> memberTicketDtos = new ArrayList<>();

        // ticket 을 member 별로 분류
        Map<Member, List<Ticket>> map = new HashMap<>();
        for (Ticket ticket : tickets) {
            Member member = ticket.getMember();

            List<Ticket> memberTickets = map.getOrDefault(member, new ArrayList<>());
            memberTickets.add(ticket);
            map.put(member, memberTickets);
        }

        // 분류된 tickets 에 대해 member 별로 dto 를 생성
        for (Member member : members) {
            List<Ticket> memberTickets = map.getOrDefault(member, new ArrayList<>());

            MemberTicketDto memberTicketDto = MemberTicketDto.from(member, memberTickets);
            memberTicketDtos.add(memberTicketDto);
        }

        return memberTicketDtos;
    }

    @Transactional
    public void expireTicketsNotExpired() {
        List<Ticket> ticketsNotExpired = ticketRepository.findTicketsByExpiredFalse();

        ticketRecordRepository.insertTicketRecords(ticketsNotExpired);
        int updateCount = ticketRepository.expireTicketsWhereExpiredFalse();

        log.info("{}개의 유효한 티켓의 상태가 종료 상태로 변경되었습니다.", updateCount);
    }
}
