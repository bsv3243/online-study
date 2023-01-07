package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.MemberCreateRequest;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Long addMember(MemberCreateRequest memberCreateRequest) {
        Member member = Member.createMember(memberCreateRequest);
        memberRepository.save(member);

        return member.getId();
    }
}
