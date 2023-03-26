package seong.onlinestudy.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.RecordDto;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.RecordsGetRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @InjectMocks
    RecordService recordService;

    @Mock
    TicketRepository ticketRepository;

    @Test
    void getRecords_조건없음() {
        //given
        List<Member> members = createMembers(50, true);
        List<Group> groups = createGroups(members, 10, true);
        List<Study> studies = createStudies(10, true);

        for(int i=groups.size(); i<members.size(); i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i % groups.size()).addGroupMember(groupMember);
        }

        List<Ticket> endTickets = createEndTickets(members, groups, studies, 1000);

        given(ticketRepository.findTickets(any(), any(), (Long)isNull(), any(), any())).willReturn(endTickets);

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setStartDate(LocalDate.now());
        request.setDays(1);
        List<StudyRecordDto> studyRecords = recordService.getRecords(request, members.get(0));

        //then
        LocalDateTime startTime = endTickets.get(0).getStartTime();
        assertThat(studyRecords).allSatisfy(studyRecord -> {
            assertThat(studyRecord.getRecords()).allSatisfy(record -> {
                assertThat(record.getStudyTime()).isEqualTo(1000L * members.size()/studies.size());
                assertThat(record.getStartTime()).isEqualToIgnoringNanos(startTime);
                assertThat(record.getEndTime()).isEqualToIgnoringNanos(startTime.plusSeconds(1000));

            });
        });
    }

    @Test
    void getRecords_스터디조건() {
        //given
        List<Member> members = createMembers(50, true);
        List<Group> groups = createGroups(members, 10, true);
        List<Study> studies = createStudies(10, true);
        List<Ticket> tickets = new ArrayList<>();

        for(int i=groups.size(); i<members.size(); i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
            groups.get(i % 10).addGroupMember(groupMember);
        }
        for(int i=0; i<50; i++) {
            Ticket ticket = createTicket(TicketStatus.STUDY, members.get(i),
                    studies.get(i % studies.size()), groups.get(i % groups.size()));
            setField(ticket, "id", (long) i);
            tickets.add(ticket);
        }

        given(ticketRepository.findTickets(any(), any(), (Long)isNull(), any(), any()))
                .willReturn(tickets.stream().filter(ticket ->
                                ticket.getStudy().equals(studies.get(0))).collect(Collectors.toList()));

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setStudyId(studies.get(0).getId());
        List<StudyRecordDto> records = recordService.getRecords(request, members.get(0));

        //then
        assertThat(records).anySatisfy(record -> {
            assertThat(record.getStudyId()).isEqualTo(studies.get(0).getId());
        });
        assertThat(records.size()).isEqualTo(1);
    }

    @Test
    void getRecords_그룹조건() {
        //given
        List<Member> members = createMembers(50, true);
        List<Group> groups = createGroups(members, 10, true);
        List<Study> studies = createStudies(10, true);
        List<Ticket> tickets = new ArrayList<>();

        for(int i=groups.size(); i<members.size(); i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
        }
        for(int i=0; i<50; i++) {
            Ticket ticket = createTicket(TicketStatus.STUDY, members.get(i),
                    studies.get(i % studies.size()), groups.get(i % groups.size()));
            setField(ticket, "id", (long) i);
            tickets.add(ticket);
        }


        List<Ticket> filteredTickets = tickets.stream().filter(ticket ->
                ticket.getGroup().equals(groups.get(0))).collect(Collectors.toList());
        given(ticketRepository.findTickets(any(), any(), (Long)isNull(), any(), any()))
                .willReturn(filteredTickets);

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setStudyId(groups.get(0).getId());
        List<StudyRecordDto> records = recordService.getRecords(request, members.get(0));

        //then
        Set<Study> filteredStudies = filteredTickets.stream()
                .map(Ticket::getStudy).collect(Collectors.toSet());
        assertThat(records.stream().map(StudyRecordDto::getStudyId).collect(Collectors.toList()))
                .containsAnyElementsOf(filteredStudies.stream().map(Study::getId).collect(Collectors.toList()));
    }
}