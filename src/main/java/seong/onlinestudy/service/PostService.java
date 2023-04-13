package seong.onlinestudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.PostDto;
import seong.onlinestudy.dto.PostStudyDto;
import seong.onlinestudy.exception.PermissionControlException;
import seong.onlinestudy.repository.*;
import seong.onlinestudy.request.post.PostCreateRequest;
import seong.onlinestudy.request.post.PostUpdateRequest;
import seong.onlinestudy.request.post.PostsDeleteRequest;
import seong.onlinestudy.request.post.PostsGetRequest;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final StudyRepository studyRepository;
    private final PostStudyRepository postStudyRepository;

    public Page<PostDto> getPosts(PostsGetRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<Post> postsWithComments
                = postRepository.findPostsWithComments(
                        request.getMemberId(),
                        request.getGroupId(),
                        request.getSearch(),
                        request.getCategory(),
                        request.getStudyIds(),
                        pageRequest);

        List<PostStudy> postStudies = postStudyRepository.findStudiesWhereInPosts(postsWithComments.getContent());

        Page<PostDto> posts = postsWithComments.map(post -> {
            PostDto postDto = PostDto.from(post);

            //postStudy 의 post.id와 post 의 id가 일치하는 것끼리 새로운 리스트 반환, 이후 dto 로 변환
            List<PostStudy> filtered = postStudies.stream()
                    .filter(postStudy -> postStudy.getPost().getId().equals(post.getId())).collect(Collectors.toList());
            List<PostStudyDto> postStudyDtos = filtered.stream().map(PostStudyDto::from).collect(Collectors.toList());

            postDto.setPostStudies(postStudyDtos);

            return postDto;
        });

        return posts;
    }

    @Transactional
    public Long createPost(PostCreateRequest request, Member loginMember) {
        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new NoSuchElementException("잘못된 접근입니다."));

        Post post = Post.createPost(request, member);

        Group group = groupRepository.findGroupWithMembers(request.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        Optional<GroupMember> groupMember = group.getGroupMembers().stream()
                .filter(gm -> gm.getMember().getId().equals(member.getId())).findFirst();
        groupMember.orElseThrow(() -> new PermissionControlException("접근 권한이 없습니다."));

        post.setGroup(group);

        if(request.getStudyIds() != null) {
            List<Study> studies = studyRepository.findAllById(request.getStudyIds());
            List<PostStudy> postStudies = studies.stream().map(study -> PostStudy.create(post, study))
                    .collect(Collectors.toList());
        }

        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    @Transactional
    public PostDto getPost(Long postId) {
        Post post = postRepository.findByIdWithMemberAndGroup(postId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));

        post.plusViewCount();

        PostDto postDto = PostDto.from(post);

        //게시글 학습 태그 리스트 조회
        List<PostStudy> postStudies = postStudyRepository.findStudiesWherePost(post);
        List<PostStudyDto> postStudyDtos = postStudies.stream()
                .map(PostStudyDto::from).collect(Collectors.toList());

        postDto.setPostStudies(postStudyDtos);

        return postDto;
    }

    @Transactional
    public Long updatePost(Long postId, PostUpdateRequest request, Member loginMember) {
        Post post = postRepository.findByIdWithStudies(postId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));

        //Post 를 생성한 회원과 로그인한 회원 정보가 일치하지 않으면
        if(!post.getMember().getId().equals(loginMember.getId())) {
            throw new PermissionControlException("해당 게시글의 수정 권한이 없습니다.");
        }

        List<Study> newStudies = studyRepository.findAllById(request.getStudyIds());

        //제거된 PostStudy 삭제
        List<PostStudy> postStudies = post.getPostStudies();
        postStudies.removeIf(postStudy -> !newStudies.contains(postStudy.getStudy()));

        //새로운 PostStudy 추가
        List<Study> oldStudies = postStudies.stream().map(PostStudy::getStudy).collect(Collectors.toList());
        for (Study newStudy : newStudies) {
            if(!oldStudies.contains(newStudy)) {
                PostStudy.create(post, newStudy);
            }
        }

        post.update(request);

        return post.getId();
    }

    @Transactional
    public void deletePost(Long postId, Member loginMember) {
        Post post = postRepository.findByIdWithStudies(postId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글입니다."));

        if (!post.getMember().getId().equals(loginMember.getId())) {
            throw new PermissionControlException("해당 게시글의 삭제 권한이 없습니다.");
        }

        postStudyRepository.deleteAllByPost(post);
        post.delete();
    }

    @Transactional
    public void deletePosts(PostsDeleteRequest request, Member loginMember) {
        if(!request.getMemberId().equals(loginMember.getId())) {
            throw new PermissionControlException("권한이 없습니다.");
        }

        postRepository.softDeleteAllByMemberId(request.getMemberId());
    }
}
