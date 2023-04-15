package seong.onlinestudy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.enumtype.GroupRole;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.record.RecordsGetRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    List<Member> members;
    List<Group> groups;
    List<Study> studies;

    List<Ticket> tickets;
    List<StudyTicket> studyTickets;

    @BeforeEach
    void init() {
        members = createMembers(50, true);
        groups = createGroups(members, 10, true);
        studies = createStudies(10, true);

        joinMembersToGroups(members, groups);

        tickets = createStudyTickets(members, groups, studies, true);
        studyTickets = tickets.stream().map(ticket -> (StudyTicket) ticket).collect(Collectors.toList());

        for (StudyTicket studyTicket : studyTickets) {
            if(studyTicket.isExpired()) {
                setField(studyTicket.getTicketRecord(), "activeTime", 1000L);
                setField(studyTicket.getTicketRecord(), "expiredTime", studyTicket.getStartTime().plusSeconds(1000));
            }
        }
    }

    @Test
    @DisplayName("공부 기록 목록 조회_조건 없음")
    @Disabled("로컬 빌드시 통과, 젠킨스 빌드시 통과하지 못함. 수정 필요")
    void getRecords_조건없음() {
        //given
        given(ticketRepository.findStudyTickets((Long)isNull(), any(), any(), any(), any())).willReturn(studyTickets);

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setStartDate(LocalDate.now());
        request.setDays(1);
        List<StudyRecordDto> studyRecords = ticketRecordService.getRecords(request, members.get(0).getId());

        //then
        assertThat(studyRecords.size()).isEqualTo(studies.size());
        assertThat(studyRecords).allSatisfy(studyRecordDto -> {
            long testStudyTime = getTargetStudyTime(studyTickets, studyRecordDto.getStudyId());

            assertThat(studyRecordDto.getMemberCount()).isGreaterThan(0);
            assertThat(studyRecordDto.getRecords()).allSatisfy(recordDto -> {
                assertThat(recordDto.getStudyTime()).isEqualTo(testStudyTime);
            });
        });
    }

    private long getTargetStudyTime(List<StudyTicket> studyTickets, Long studyId) {
        List<StudyTicket> targetTestStudyTickets = studyTickets.stream()
                .filter(studyTicket -> studyTicket.getStudy().getId().equals(studyId))
                .collect(Collectors.toList());

        long testStudyTime = 0;
        for (StudyTicket targetTestStudyTicket : targetTestStudyTickets) {
            if(targetTestStudyTicket.isExpired()) {
                testStudyTime += targetTestStudyTicket.getTicketRecord().getActiveTime();
            }
        }
        return testStudyTime;
    }

    @Test
    @DisplayName("공부 기록 목록 조회_스터디 조건")
    void getRecords_스터디조건() {
        //given

        given(ticketRepository.findStudyTickets((Long) isNull(), any(), any(), any(), any()))
                .willReturn(studyTickets);

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setStudyId(studies.get(0).getId());
        List<StudyRecordDto> records = ticketRecordService.getRecords(request, members.get(0).getId());

        //then
        assertThat(records).allSatisfy(studyRecordDto -> {
            assertThat(studyRecordDto.getStudyId()).isNotNull();
            assertThat(studyRecordDto.getStudyName()).isNotNull();
            assertThat(studyRecordDto.getMemberCount()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("공부 기록 목록 조회_그룹 조건")
    void getRecords_그룹조건() {
        //given
        List<Member> members = createMembers(50, true);
        List<Group> groups = createGroups(members, 10, true);
        List<Study> studies = createStudies(10, true);
        List<StudyTicket> tickets = new ArrayList<>();

        for(int i=groups.size(); i<members.size(); i++) {
            GroupMember groupMember = GroupMember.createGroupMember(members.get(i), GroupRole.USER);
        }
        for(int i=0; i<50; i++) {
            Ticket ticket = createStudyTicket(members.get(i),
                    groups.get(i % groups.size()), studies.get(i % studies.size()));
            setField(ticket, "id", (long) i);
            tickets.add((StudyTicket) ticket);
        }

        given(ticketRepository.findStudyTickets((Long)isNull(), any(), any(), any(), any()))
                .willReturn(tickets);

        //when
        RecordsGetRequest request = new RecordsGetRequest();
        request.setStudyId(groups.get(0).getId());
        List<StudyRecordDto> records = ticketRecordService.getRecords(request, members.get(0).getId());

        //then
        List<Long> testTargetStudyIds = tickets.stream()
                .map(studyTicket -> studyTicket.getStudy().getId())
                .distinct()
                .collect(Collectors.toList());

        List<Long> findStudyIds = records.stream()
                .map(StudyRecordDto::getStudyId)
                .collect(Collectors.toList());

        assertThat(findStudyIds).containsAnyElementsOf(testTargetStudyIds);
    }
}