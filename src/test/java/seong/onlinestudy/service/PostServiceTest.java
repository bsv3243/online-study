package seong.onlinestudy.service;

import lombok.extern.slf4j.Slf4j;
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
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.PostRepository;
import seong.onlinestudy.repository.PostStudyRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.request.PostCreateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    PostService postService;

    @Mock
    PostRepository postRepository;
    @Mock
    GroupRepository groupRepository;
    @Mock
    StudyRepository studyRepository;
    @Mock
    PostStudyRepository postStudyRepository;

    @Test
    void createPost() {
        //given
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("test");
        request.setContent("test");
        request.setCategory(PostCategory.CHAT);

        Member member = MyUtils.createMember("test1234", "test1234");
        setField(member, "id", 1L);
        Post post = Post.createPost(request, member);
        setField(post, "id", 1L);

        Group group = MyUtils.createGroup("테스트그룹", 30, member);
        setField(group, "id", 1L);
        request.setGroupId(group.getId());

        List<Study> studies = MyUtils.createStudies(5, false);
        List<PostStudy> postStudies = new ArrayList<>();
        for(int i=0; i<=5; i++) {
            setField(studies.get(i), "id", (long) i);
            postStudies.add(PostStudy.create(post, studies.get(i)));
        }
        request.setStudyIds(studies.stream().map(Study::getId).collect(Collectors.toList()));

        given(postRepository.save(any())).willReturn(post);
        if(request.getGroupId() != null) {
            post.setGroup(group);
            given(groupRepository.findGroupWithMembers(any())).willReturn(Optional.of(group));
        }
        if(request.getStudyIds() != null) {
            given(studyRepository.findAllById(any())).willReturn(studies);
        }

        //when
        Long postId = postService.createPost(request, member);

        //then
        assertThat(postId).isEqualTo(post.getId());
    }

    @Test
    void getPost() {
        //given
        Member member = MyUtils.createMember("tester", "tester");
        setField(member, "id", 1L);

        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        setField(post, "id", 1L);

        given(postRepository.findByIdWithMemberAndGroup(any())).willReturn(Optional.of(post));

        List<Study> studies = MyUtils.createStudies(5, false);
        List<PostStudy> postStudies = new ArrayList<>();
        for(int i=0; i<=5; i++) {
            setField(studies.get(i), "id", (long)i);

            PostStudy postStudy = PostStudy.create(post, studies.get(i));
            setField(postStudy, "id", (long)i);

            postStudies.add(postStudy);
            log.info("id={}", postStudy.getId());
        }
        given(postStudyRepository.findStudiesWherePost(any())).willReturn(postStudies);

        //when
        PostDto postDto = postService.getPost(post.getId());

        //then
        assertThat(postDto.getPostId()).isEqualTo(post.getId());
        assertThat(postDto.getTitle()).isEqualTo(post.getTitle());
        assertThat(postDto.getMember().getUsername()).isEqualTo(member.getUsername());

        List<PostStudyDto> postStudyDtos = postStudies.stream()
                .map(PostStudyDto::from).collect(Collectors.toList());
        assertThat(postDto.getPostStudies()).containsAll(postStudyDtos);
    }

    @Test
    void getPosts() {
        //given
        List<Member> members = createMembers(5, false);
        List<Group> groups = createGroups(members, 5, true);
        List<Post> posts = createPosts(members, groups, 30, true);

        int page = 0;
        int size = 10;
        Long groupId = null;
        String search = null;
        PostCategory category = null;
        List<Long> studyIds = null;

        List<Post> subList = posts.subList(0, 10);
        given(postRepository.findPostsWithComments(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(subList, PageRequest.of(page, size), posts.size()));

        //when
        Page<PostDto> postDtos = postService.getPosts(page, size, groupId, search, category, studyIds);

        //then
        List<PostDto> postDtoList = subList.stream().map(PostDto::from).collect(Collectors.toList());

        List<PostDto> content = postDtos.getContent();
        assertThat(content).containsExactlyInAnyOrderElementsOf(postDtoList);
    }

    @Test
    void getPosts_withStudies() {
        //given
        List<Member> members = createMembers(5, false);
        List<Group> groups = createGroups(members, 5, true);
        List<Post> posts = createPosts(members, groups, 5, true);
        List<Study> studies = createStudies(30, true);
        for(int i=0; i<30; i++) {
            PostStudy postStudy = PostStudy.create(posts.get(i%5), studies.get(i));
            setField(postStudy, "id", (long) i);
        }

        int page = 0;
        int size = 10;
        Long groupId = null;
        String search = null;
        PostCategory category = null;
        List<Long> studyIds = null;

        given(postRepository.findPostsWithComments(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(posts, PageRequest.of(page, size), posts.size()));

        //when
        Page<PostDto> postDtos = postService.getPosts(page, size, groupId, search, category, studyIds);

        //then
        List<PostDto> postDtoList = posts.stream().map(PostDto::from).collect(Collectors.toList());

        List<PostDto> content = postDtos.getContent();
        assertThat(content).containsExactlyInAnyOrderElementsOf(postDtoList);
    }

}