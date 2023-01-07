package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
