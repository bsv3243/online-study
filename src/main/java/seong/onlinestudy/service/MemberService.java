package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.Member;
import seong.onlinestudy.dto.MemberDto;
import seong.onlinestudy.exception.DuplicateElementException;
import seong.onlinestudy.repository.GroupMemberRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.request.member.MemberCreateRequest;
import seong.onlinestudy.request.member.MemberDuplicateCheckRequest;
import seong.onlinestudy.request.member.MemberUpdateRequest;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public Long createMember(MemberCreateRequest memberCreateRequest) {
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

    public MemberDto getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return MemberDto.from(member);
    }

    @Transactional
    public Long updateMember(Long memberId, MemberUpdateRequest request) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        findMember.update(request);

        return findMember.getId();
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회웝입니다."));

        findMember.delete();

        groupMemberRepository.deleteAllByMemberIdRoleIsNotMaster(memberId);
    }
}
