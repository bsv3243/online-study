package seong.onlinestudy.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.dto.GroupDto;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.enumtype.OrderBy;

import java.util.List;

public interface GroupRepositoryCustom {

    Page<Group> findGroups(Long memberId, GroupCategory category, String search, List<Long> studyIds, OrderBy orderBy, Pageable pageable);

    Page<GroupDto> findGroupsAndMapToGroupDto(Long memberId, GroupCategory category, String search,
                                              List<Long> studyIds, OrderBy orderBy, Pageable pageable);
}
