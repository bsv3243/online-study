package seong.onlinestudy.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.PostDto;
import seong.onlinestudy.dto.PostStudyDto;
import seong.onlinestudy.enumtype.PostCategory;
import seong.onlinestudy.repository.*;
import seong.onlinestudy.request.post.PostCreateRequest;
import seong.onlinestudy.request.post.PostUpdateRequest;
import seong.onlinestudy.request.post.PostsGetRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    PostService postService;

    @Mock
    MemberRepository memberRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    GroupRepository groupRepository;
    @Mock
    StudyRepository studyRepository;
    @Mock
    PostStudyRepository postStudyRepository;

    List<Member> members;
    List<Group> groups;
    List<Study> studies;
    List<Post> posts;

    @BeforeEach
    void init() {
        members = createMembers(20, true);
        groups = createGroups(members, 2, true);

        joinMembersToGroups(members, groups);

        studies = createStudies(3, true);
        posts = createPosts(members, groups, 13, true);
    }

    @Test
    @DisplayName("게시글 생성")
    void createPost() {
        //given
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("test");
        request.setContent("test");
        request.setCategory(PostCategory.CHAT);

        Member testMember = members.get(0);
        Group testGroup = groups.get(0);

        Post testPost = Post.createPost(request, testMember);
        setField(testPost, "id", 1L);

        given(memberRepository.findById(any())).willReturn(Optional.of(testMember));
        given(groupRepository.findGroupWithMembers(any())).willReturn(Optional.of(testGroup));
        given(postRepository.save(any())).willReturn(testPost);

        //when
        Long postId = postService.createPost(request, testMember);

        //then
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void getPost() {
        //given
        Post testPost = posts.get(0);
        Member testMember = testPost.getMember();

        given(postRepository.findByIdWithMemberAndGroup(any()))
                .willReturn(Optional.of(testPost));

        List<PostStudy> postStudies = new ArrayList<>();
        for (Study study : studies) {
            PostStudy postStudy = PostStudy.create(testPost, study);
            postStudies.add(postStudy);
        }

        given(postStudyRepository.findStudiesWherePost(any())).willReturn(postStudies);

        //when
        PostDto postDto = postService.getPost(testPost.getId());

        //then
        assertThat(postDto.getPostId()).isEqualTo(testPost.getId());
        assertThat(postDto.getTitle()).isEqualTo(testPost.getTitle());
        assertThat(postDto.getMember().getUsername()).isEqualTo(testMember.getUsername());

        List<PostStudyDto> postStudyDtos = postStudies.stream()
                .map(PostStudyDto::from).collect(Collectors.toList());
        assertThat(postDto.getPostStudies()).containsExactlyInAnyOrderElementsOf(postStudyDtos);
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void getPosts() {
        //given
        List<Member> members = createMembers(5, false);
        List<Group> groups = createGroups(members, 5, true);
        List<Post> posts = createPosts(members, groups, 30, true);

        int page = 0;
        int size = 10;

        PostsGetRequest request = new PostsGetRequest();


        List<Post> subList = posts.subList(0, 10);
        given(postRepository.findPostsWithComments(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(subList, PageRequest.of(page, size), posts.size()));

        //when
        Page<PostDto> postDtos = postService.getPosts(request);

        //then
        List<PostDto> postDtoList = subList.stream().map(PostDto::from).collect(Collectors.toList());

        List<PostDto> content = postDtos.getContent();
        assertThat(content).containsExactlyInAnyOrderElementsOf(postDtoList);
    }

    @Test
    @DisplayName("게시글 목록 조회(스터디 포함 검증)")
    void getPosts_withStudies() {
        //given
        List<Member> members = createMembers(5, false);
        List<Group> groups = createGroups(members, 5, true);
        List<Post> posts = createPosts(members, groups, 5, true);
        List<Study> studies = createStudies(30, true);

        List<PostStudy> postStudies = new ArrayList<>();
        for(int i=0; i<30; i++) {
            PostStudy postStudy = PostStudy.create(posts.get(i%5), studies.get(i));
            setField(postStudy, "id", (long) i);

            postStudies.add(postStudy);
        }

        int page = 0;
        int size = 10;

        PostsGetRequest request = new PostsGetRequest();

        given(postRepository.findPostsWithComments(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(posts, PageRequest.of(page, size), posts.size()));
        given(postStudyRepository.findStudiesWhereInPosts(anyList()))
                .willReturn(postStudies);

        //when
        Page<PostDto> testPostDtosWithPage = postService.getPosts(request);

        //then
        List<PostDto> testPostDtos = testPostDtosWithPage.getContent();
        assertThat(testPostDtos).allSatisfy(postDto -> {
            List<PostStudy> targetPostStudies = postStudies.stream().filter(postStudy -> postStudy.getPost().getId().equals(postDto.getPostId()))
                    .collect(Collectors.toList());
            List<Long> targetPostStudyIds = targetPostStudies.stream().map(PostStudy::getId).collect(Collectors.toList());

            List<Long> findPostStudyIds = postDto.getPostStudies().stream().map(PostStudyDto::getPostStudyId).collect(Collectors.toList());

            assertThat(findPostStudyIds).containsExactlyInAnyOrderElementsOf(targetPostStudyIds);
        });
    }

    @Test
    @DisplayName("게시글 업데이트")
    void updatePost() {
        //given
        Member member = createMember("member", "member");
        setField(member, "id", 1L);

        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        setField(post, "id", 1L);

        List<Study> studies = createStudies(7, true);

        List<Study> oldStudies = studies.subList(0, 4);
        List<PostStudy> oldPostStudies = new ArrayList<>();
        for(int i=0; i<oldStudies.size(); i++) {
            PostStudy postStudy = PostStudy.create(post, oldStudies.get(i));
            setField(postStudy, "id", (long) i);
            oldPostStudies.add(postStudy);
        }

        List<Study> newStudies = studies.subList(4, 7);
        Long postId = post.getId();
        Member loginMember = member;
        PostUpdateRequest request = new PostUpdateRequest();
        request.setContent("test1234");request.setTitle("test1234");
        request.setStudyIds(newStudies.stream()
                .map(Study::getId).collect(Collectors.toList()));

        given(postRepository.findByIdWithStudies(any())).willReturn(Optional.of(post));
        given(studyRepository.findAllById(any())).willReturn(newStudies);

        //when
        Long updatePostId = postService.updatePost(postId, request, loginMember);

        //then
        assertThat(post.getTitle()).isEqualTo(request.getTitle());
        assertThat(post.getContent()).isEqualTo(request.getContent());
        List<Study> result = post.getPostStudies().stream()
                .map(PostStudy::getStudy).collect(Collectors.toList());
        assertThat(result).isEqualTo(newStudies);
    }

}