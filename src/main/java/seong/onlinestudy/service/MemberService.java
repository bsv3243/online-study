package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.MemberCreateRequest;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Long addMember(MemberCreateRequest memberCreateRequest) {
        Member member = Member.createMember(memberCreateRequest);
        memberRepository.save(member);

        return member.getId();
    }
}
