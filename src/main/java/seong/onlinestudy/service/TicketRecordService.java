package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seong.onlinestudy.constant.TimeConst;
import seong.onlinestudy.domain.StudyTicket;
import seong.onlinestudy.dto.RecordDto;
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

    public List<StudyRecordDto> getRecords(RecordsGetRequest request) {

        LocalDateTime startTime = request.getStartDate().atStartOfDay().plusHours(TimeConst.DAY_START);
        LocalDateTime endTime = startTime.plusDays(request.getDays());

        List<StudyTicket> findTickets = ticketRepository
                .findStudyTickets(request.getMemberId(), request.getGroupId(), request.getStudyId(), startTime, endTime);

        Map<Study, List<Ticket>> ticketsGroupByStudy = getTicketsGroupByStudy(findTickets);
        List<StudyRecordDto> studyRecordDtos = getStudyRecordDtos(ticketsGroupByStudy, request);

        return studyRecordDtos;
    }

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

    private List<StudyRecordDto> getStudyRecordDtos(Map<Study, List<Ticket>> ticketsGroupByStudy,
                                                    RecordsGetRequest request) {
        List<StudyRecordDto> studyRecordDtos = new ArrayList<>();
        for(Study study: ticketsGroupByStudy.keySet()) {
            List<Ticket> tickets = ticketsGroupByStudy.get(study);

            Map<LocalDate, RecordDto> recordsGroupByDate = getRecordDtosGroupByDate(tickets, request);

            StudyRecordDto studyRecord = StudyRecordDto.from(study, recordsGroupByDate, request);
            studyRecordDtos.add(studyRecord);
        }
        return studyRecordDtos;
    }

    private Map<LocalDate, RecordDto> getRecordDtosGroupByDate(List<Ticket> tickets, RecordsGetRequest request) {
        Map<LocalDate, RecordDto> recordsGroupByDate = putEachDateWithInitRecordDtos(request);

        for(Ticket ticket: tickets) {
            LocalDate publishDate = ticket.getDateBySetting();

            RecordDto recordDto = recordsGroupByDate.get(publishDate);
            recordDto.changeFirstStartTimeAndLastEndTime(ticket);
            recordDto.addStudyTime(ticket);

            recordsGroupByDate.put(publishDate, recordDto);
        }
        return recordsGroupByDate;
    }

    private Map<LocalDate, RecordDto> putEachDateWithInitRecordDtos(RecordsGetRequest request) {
        Map<LocalDate, RecordDto> recordsGroupByDate = new HashMap<>();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getStartDate().plusDays(request.getDays());
        for(LocalDate date=startDate; date.isBefore(endDate); date=date.plusDays(1)) {
            recordsGroupByDate.put(date, RecordDto.from(date));
        }
        return recordsGroupByDate;
    }
}
