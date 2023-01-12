package seong.onlinestudy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Group;
import seong.onlinestudy.domain.GroupCategory;
import seong.onlinestudy.dto.GroupDto;

import java.util.List;

public interface GroupRepositoryCustom {

    Page<GroupDto> getGroups(Pageable pageable, GroupCategory category, String search);
}
