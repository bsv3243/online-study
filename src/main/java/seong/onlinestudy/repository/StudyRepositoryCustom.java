package seong.onlinestudy.repository;

import seong.onlinestudy.domain.Group;
import seong.onlinestudy.dto.GroupStudyDto;

import java.util.List;

public interface StudyRepositoryCustom {

    List<GroupStudyDto> findStudiesInGroups(List<Group> groups);
}
