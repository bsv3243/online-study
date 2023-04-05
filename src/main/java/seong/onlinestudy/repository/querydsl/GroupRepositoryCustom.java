package seong.onlinestudy.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.enumtype.OrderBy;

import java.util.List;

public interface GroupRepositoryCustom {

    Page<Group> findGroups(Pageable pageable, GroupCategory category, String search, List<Long> studyIds, OrderBy orderBy);
}
