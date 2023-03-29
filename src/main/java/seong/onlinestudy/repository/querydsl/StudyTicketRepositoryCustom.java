package seong.onlinestudy.repository.querydsl;

import seong.onlinestudy.domain.StudyTicket;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyTicketRepositoryCustom {

    List<StudyTicket> findStudyTickets(Long memberId, Long groupId, Long studyId, LocalDateTime startTime, LocalDateTime endTime);
}
