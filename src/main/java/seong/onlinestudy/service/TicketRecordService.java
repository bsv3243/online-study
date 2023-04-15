package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seong.onlinestudy.TimeConst;
import seong.onlinestudy.domain.StudyTicket;
import seong.onlinestudy.dto.RecordDto;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.request.record.RecordsGetRequest;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.domain.Ticket;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.repository.TicketRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TicketRecordService {

    private final TicketRepository ticketRepository;

    public List<StudyRecordDto> getRecords(RecordsGetRequest request, Long loginMemberId) {
        if(loginMemberId != null) {
            //요청 조건 중 memberId가 있을 경우 로그인한 회원의 id 와 비교
            if (request.getMemberId() != null && !loginMemberId.equals(request.getMemberId())) {
                throw new PermissionControlException("권한이 없습니다.");
            }
        }
        LocalDateTime startTime = request.getStartDate().atStartOfDay().plusHours(TimeConst.DAY_START);
        LocalDateTime endTime = startTime.plusDays(request.getDays());

        List<StudyTicket> findTickets = ticketRepository
                .findStudyTickets(request.getMemberId(), request.getGroupId(), request.getStudyId(), startTime, endTime);

        List<StudyRecordDto> studyRecordDtos = getStudyRecordDtos(request, findTickets);

        return studyRecordDtos;
    }

    private List<StudyRecordDto> getStudyRecordDtos(RecordsGetRequest request, List<StudyTicket> findTickets) {
        Map<Study, List<Ticket>> ticketsGroupByStudy = getTicketsGroupByStudy(findTickets);

        List<StudyRecordDto> studyRecordDtos = createStudyRecordsFromTickets(ticketsGroupByStudy, request);
        return studyRecordDtos;
    }

    //스터디 별로 티켓을 분류한다.
    private Map<Study, List<Ticket>> getTicketsGroupByStudy(List<StudyTicket> findTickets) {
        Map<Study, List<Ticket>> ticketsGroupByStudy = new HashMap<>();
        for (StudyTicket findTicket : findTickets) {
            Study findStudy = findTicket.getStudy();

            List<Ticket> filteredTickets = ticketsGroupByStudy.getOrDefault(findStudy, new ArrayList<>());
            filteredTickets.add(findTicket);
            ticketsGroupByStudy.put(findStudy, filteredTickets);
        }
        return ticketsGroupByStudy;
    }

    private List<StudyRecordDto> createStudyRecordsFromTickets(Map<Study, List<Ticket>> ticketsGroupByStudy,
                                                               RecordsGetRequest request) {
        List<StudyRecordDto> studyRecordDtos = new ArrayList<>();
        for (Study study : ticketsGroupByStudy.keySet()) {
            List<Ticket> filteredTickets = ticketsGroupByStudy.get(study);

            //스터디별로 분류된 Ticket 들을 발행된 date 에 따라 하나의 RecordDto 로 뭉친다.
            Map<LocalDate, RecordDto> recordsGroupByDate = getRecordsGroupByDate(filteredTickets);

            //date 별로 뭉쳐진 RecordDto 들을 data 로 가지는 studyRecord 를 만든다.
            StudyRecordDto studyRecord = createStudyRecord(study, recordsGroupByDate, request);
            studyRecordDtos.add(studyRecord);
        }
        return studyRecordDtos;
    }

    private Map<LocalDate, RecordDto> getRecordsGroupByDate(List<Ticket> filteredTickets) {
        Map<LocalDate, RecordDto> recordsGroupByDate = new HashMap<>();
        for (Ticket ticket : filteredTickets) {
            LocalDate publishDate = ticket.getDateBySetting();

            RecordDto recordDto = recordsGroupByDate.getOrDefault(publishDate, RecordDto.from(ticket));
            if(recordsGroupByDate.containsValue(recordDto)) { //새롭게 만든 recordDto 가 아닐 때
                recordDto.addStudyTime(ticket);
                recordDto.compareStartAndEndTime(ticket);
            }

            recordsGroupByDate.put(publishDate, recordDto);
        }
        return recordsGroupByDate;
    }

    private StudyRecordDto createStudyRecord(Study study,
                                             Map<LocalDate, RecordDto> recordsGroupByDate,
                                             RecordsGetRequest request) {
        StudyRecordDto studyRecord = StudyRecordDto.from(study);
        for(int i = 0; i< request.getDays(); i++) {
            LocalDate startDate = request.getStartDate().plusDays(i);

            RecordDto recordDto = recordsGroupByDate.getOrDefault(startDate, RecordDto.from(startDate));
            studyRecord.addRecord(recordDto);
        }

        studyRecord.updateMemberCount();
        return studyRecord;
    }
}
