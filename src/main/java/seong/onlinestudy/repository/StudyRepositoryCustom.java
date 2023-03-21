package seong.onlinestudy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.Study;
import seong.onlinestudy.dto.GroupStudyDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyRepositoryCustom {

    List<GroupStudyDto> findStudiesInGroups(List<Group> groups);

    Page<Study> findStudies(Long memberId, Long groupId, String search, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
