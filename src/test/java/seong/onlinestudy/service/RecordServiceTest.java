package seong.onlinestudy.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.RecordsGetRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void getRecords_스터디조건() {
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


        given(ticketRepository.findTickets(any(), any(), any(), any(), any()))
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
        given(ticketRepository.findTickets(any(), any(), any(), any(), any()))
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