package seong.onlinestudy.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.RecordDto;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.enumtype.GroupRole;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.record.RecordsGetRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@ExtendWith(MockitoExtension.class)
class TicketRecordServiceTest {

    @InjectMocks
    TicketRecordService ticketRecordService;

    @Mock
    TicketRepository ticketRepository;

    @Test
    public void getRecords_day1() {
        //given
        List<Member> members = createMembers(5);
        Group group = createGroup("group", 30, members.get(0));
        for(int i=1; i<members.size(); i++) {
            group.addGroupMember(GroupMember.createGroupMember(members.get(i), GroupRole.USER));
        }
        Study studyA = createStudy("studyA");
        Study studyB = createStudy("studyB");
        List<Study> studies = List.of(studyA, studyB);

        Map<Study, Long> ticketPublishCount = new HashMap<>();
        ticketPublishCount.put(studyA, 3L);
        ticketPublishCount.put(studyB, 2L);

        List<StudyTicket> tickets = new ArrayList<>();
        for (Member member : members) {
            LocalDateTime startTime = LocalDateTime.now().minusHours(5);
            for(int i=0; i<ticketPublishCount.get(studyA); i++) {
                StudyTicket studyTicket = createExpiredStudyTicket(member, group, studyA, startTime.plusHours(1), 1);

                tickets.add(studyTicket);
            }

            for(int i=0; i<ticketPublishCount.get(studyB); i++) {
                StudyTicket studyTicket = createExpiredStudyTicket(member, group, studyB, startTime.plusHours(1), 1);

                tickets.add(studyTicket);
            }
        }

        given(ticketRepository.findStudyTickets(any(), any(), any(), any(), any()))
                .willReturn(tickets);

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setDays(1);
        request.setStartDate(LocalDate.now());

        List<StudyRecordDto> studyRecordDtos = ticketRecordService.getRecords(request);

        //then
        assertThat(studyRecordDtos.size()).isEqualTo(2);

        List<String> studyNames = studyRecordDtos.stream()
                .map(StudyRecordDto::getStudyName)
                .collect(Collectors.toList());
        assertThat(studyNames).containsExactlyInAnyOrderElementsOf(List.of(studyA.getName(), studyB.getName()));

        assertThat(studyRecordDtos).allSatisfy(studyRecordDto -> {
            RecordDto recordDto = studyRecordDto.getRecords().get(request.getDays()-1);
            if (studyRecordDto.getStudyName().equals(studyA.getName())) {
                assertThat(recordDto.getStudyTime()).isEqualTo(ticketPublishCount.get(studyA) * members.size() * 3600);

            } else if (studyRecordDto.getStudyName().equals(studyB.getName())) {
                assertThat(recordDto.getStudyTime()).isEqualTo(ticketPublishCount.get(studyB) * members.size() * 3600);
            }
        });
    }

    @Test
    public void getRecords_day7() {
        //given
        List<Member> members = createMembers(5);
        Group group = createGroup("group", 30, members.get(0));
        for(int i=1; i<members.size(); i++) {
            group.addGroupMember(GroupMember.createGroupMember(members.get(i), GroupRole.USER));
        }
        Study studyA = createStudy("studyA");
        Study studyB = createStudy("studyB");
        List<Study> studies = List.of(studyA, studyB);

        Map<Study, Long> ticketPublishCount = new HashMap<>();
        ticketPublishCount.put(studyA, 3L);
        ticketPublishCount.put(studyB, 2L);

        List<StudyTicket> tickets = new ArrayList<>();
        for (Member member : members) {
            LocalDateTime startTime = LocalDateTime.now().minusHours(5);
            for(int i=0; i<ticketPublishCount.get(studyA); i++) {
                StudyTicket studyTicket = createExpiredStudyTicket(member, group, studyA, startTime.plusHours(1), 1);

                tickets.add(studyTicket);
            }

            for(int i=0; i<ticketPublishCount.get(studyB); i++) {
                StudyTicket studyTicket = createExpiredStudyTicket(member, group, studyB, startTime.plusHours(1), 1);

                tickets.add(studyTicket);
            }
        }

        given(ticketRepository.findStudyTickets(any(), any(), any(), any(), any()))
                .willReturn(tickets);

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setDays(7);
        request.setStartDate(LocalDate.now().minusDays(6));

        List<StudyRecordDto> studyRecordDtos = ticketRecordService.getRecords(request);

        //then
        assertThat(studyRecordDtos.size()).isEqualTo(2);

        List<String> studyNames = studyRecordDtos.stream()
                .map(StudyRecordDto::getStudyName)
                .collect(Collectors.toList());
        assertThat(studyNames).containsExactlyInAnyOrderElementsOf(List.of(studyA.getName(), studyB.getName()));

        assertThat(studyRecordDtos).allSatisfy(studyRecordDto -> {
            assertThat(studyRecordDto.getMemberCount()).isEqualTo(members.size());

            assertThat(studyRecordDto.getRecords()).allSatisfy(recordDto -> {
                if(recordDto.getDate().equals(LocalDate.now())) {  //현재 테스트 데이터는 LocalDate.now()만 이용하였음
                    if (studyRecordDto.getStudyName().equals(studyA.getName())) {
                        assertThat(recordDto.getStudyTime())
                                .isEqualTo(ticketPublishCount.get(studyA) * members.size() * 3600);

                    } else if (studyRecordDto.getStudyName().equals(studyB.getName())) {
                        assertThat(recordDto.getStudyTime())
                                .isEqualTo(ticketPublishCount.get(studyB) * members.size() * 3600);
                    }
                } else {
                    assertThat(recordDto.getStartTime()).isNull();
                    assertThat(recordDto.getEndTime()).isNull();
                    assertThat(recordDto.getStudyTime()).isEqualTo(0);
                    assertThat(recordDto.getMemberCount()).isEqualTo(0);
                }
            });
        });

    }

    private StudyTicket createExpiredStudyTicket(Member member, Group group, Study studyA, LocalDateTime startTime, int hour) {
        StudyTicket studyTicket = (StudyTicket) StudyTicket.createStudyTicket(member, group, studyA);
        studyTicket.expireAndCreateRecord();
        ReflectionTestUtils.setField(studyTicket, "startTime", startTime);
        ReflectionTestUtils.setField(studyTicket.getTicketRecord(), "expiredTime", startTime.plusHours(hour));
        ReflectionTestUtils.setField(studyTicket.getTicketRecord(), "activeTime", hour*3600);
        return studyTicket;
    }
}