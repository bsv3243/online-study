package seong.onlinestudy.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Member;

import java.time.LocalDateTime;

public interface MemberRepositoryCustom {

    Page<Member> findMembersOrderByStudyTime(Long memberId, Long groupId, LocalDateTime startTime, LocalDateTime endTime,
                                             Pageable pageable);
}
