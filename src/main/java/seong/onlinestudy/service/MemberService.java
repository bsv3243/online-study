package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.MemberDto;
import seong.onlinestudy.exception.DuplicateElementException;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.request.member.MemberDuplicateCheckRequest;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Long addMember(MemberCreateRequest memberCreateRequest) {
        memberRepository.findByUsername(memberCreateRequest.getUsername())
                .ifPresent(member -> {
                    throw new DuplicateElementException("이미 존재하는 아이디입니다.");
                });
        Member member = Member.createMember(memberCreateRequest);
        memberRepository.save(member);

        return member.getId();
    }

    public void duplicateCheck(MemberDuplicateCheckRequest request) {
        memberRepository.findByUsername(request.getUsername())
                .ifPresent(member -> {
                    throw new DuplicateElementException("이미 존재하는 아이디입니다.");
                });
    }

    public MemberDto getMember(Member loginMember) {
        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new NoSuchElementException("잘못된 접근입니다."));

        return MemberDto.from(member);
    }
}
