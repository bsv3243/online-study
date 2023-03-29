package seong.onlinestudy.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.StudyDto;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.request.study.StudiesGetRequest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @InjectMocks
    StudyService studyService;

    @Mock
    StudyRepository studyRepository;

    @Test
    @DisplayName("스터디 목록 조회_검색어 조건")
    void getStudies_검색어있음() {
        Member member = createMember("member", "member");
        setField(member, "id", 1L);

        List<Study> studies = MyUtils.createStudies(10, true);
        PageRequest pageRequest = PageRequest.of(0, 10);

        PageImpl<Study> memberStudies = new PageImpl<>(studies.subList(0, 3), pageRequest, 3);
        PageImpl<Study> allStudies = new PageImpl<>(studies.subList(3, 10), pageRequest, 7);

//        given(studyRepository.findStudiesByMember(any(), any(), any(), any())).willReturn(memberStudies);
        given(studyRepository.findStudies(any(), any(), any(), any(), any(), any())).willReturn(allStudies);

        //when
        StudiesGetRequest request = new StudiesGetRequest();
        request.setName("테스트");
        Page<StudyDto> findStudies = studyService.getStudies(request, member);

        //then
        assertThat(findStudies.getContent().stream()
                        .map(StudyDto::getName).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(allStudies.getContent().stream()
                        .map(Study::getName).collect(Collectors.toList()));
    }

    @Test
    @DisplayName("티켓 목록 조회_조건 없음")
    void getStudies_검색어없음() {
        Member member = createMember("member", "member");
        setField(member, "id", 1L);

        List<Study> studies = MyUtils.createStudies(10, true);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Group group = createGroup("group", 30, member);

        for(int i=0; i<3; i++) {
            Ticket ticket = createStudyTicket(member, group, studies.get(i));
            MyUtils.setTicketEnd(ticket, 3600);
        }

        PageImpl<Study> memberStudies = new PageImpl<>(studies.subList(0, 3), pageRequest, 3);
        PageImpl<Study> allStudies = new PageImpl<>(studies.subList(3, 10), pageRequest, 7);

        given(studyRepository.findStudies(any(), any(), any(), any(), any(), any())).willReturn(memberStudies);
//        given(studyRepository.findAllByNameContains(any(), any())).willReturn(allStudies);

        //when
        StudiesGetRequest request = new StudiesGetRequest();
        request.setName("");
        Page<StudyDto> findStudies = studyService.getStudies(request, member);

        //then
        List<StudyDto> studyDtos = findStudies.getContent();
        //studyDtos 의 스터디 이름 리스트가 타켓의 이름 리스트와 같다.
        assertThat(studyDtos.stream().map(StudyDto::getName).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(memberStudies.getContent().stream()
                        .map(Study::getName).collect(Collectors.toList()));
    }
}