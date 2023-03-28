package seong.onlinestudy.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.MyUtils.*;

@DataJpaTest
class PostStudyRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    PostStudyRepository postStudyRepository;

    List<Member> members;
    List<Group> groups;
    List<Study> studies;
    List<Post> posts;

    @BeforeEach
    void init() {
        members = createMembers(30);
        groups = createGroups(members, 3);

        joinMembersToGroups(members, groups);

        studies = createStudies(3);
        posts = createPosts(members, groups, 30, false);

        memberRepository.saveAll(members);
        groupRepository.saveAll(groups);
        postRepository.saveAll(posts);
    }

    @Test
    void findStudiesWherePost() {
        //given
        Member member = MyUtils.createMember("test1234", "test1234");
        memberRepository.save(member);

        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        postRepository.save(post);

        List<Study> studies = MyUtils.createStudies(5, false);
        studyRepository.saveAll(studies);

        List<PostStudy> postStudies = MyUtils.createPostStudies(post, studies);

        //when
        List<PostStudy> findPostStudies = postStudyRepository.findStudiesWherePost(post);

        //then
        assertThat(findPostStudies).containsAll(postStudies);
    }

    @Test
    void findStudiesWhereInPosts() {
        //given
        Member member = MyUtils.createMember("test1234", "test1234");
        memberRepository.save(member);

        Group group = MyUtils.createGroup("테스트그룹", 30, member);
        groupRepository.save(group);

        List<Post> posts = MyUtils.createPosts(List.of(member), List.of(group), 5, false);
        postRepository.saveAll(posts);

        List<Study> studies = MyUtils.createStudies(30, false);
        studyRepository.saveAll(studies);

        List<PostStudy> postStudies = new ArrayList<>();
        for(int i=0; i<5; i++) {
            PostStudy postStudy = PostStudy.create(posts.get(i), studies.get(i % 10));
            postStudies.add(postStudy);
        }

        //when
        List<PostStudy> findPostStudies = postStudyRepository.findStudiesWhereInPosts(posts);

        //then
        assertThat(findPostStudies).containsExactlyInAnyOrderElementsOf(postStudies);
    }

    @Test
    void findStudiesWhereInPosts_duplicateStudies() {
        //given
        Member member = MyUtils.createMember("test1234", "test1234");
        memberRepository.save(member);

        Group group = MyUtils.createGroup("테스트그룹", 30, member);
        groupRepository.save(group);

        Post post1 = MyUtils.createPost("테스트", "테스트", PostCategory.CHAT, member);
        Post post2 = MyUtils.createPost("테스트", "테스트", PostCategory.CHAT, member);
        postRepository.save(post1);
        postRepository.save(post2);

        List<Study> studies = MyUtils.createStudies(4, false);
        studyRepository.saveAll(studies);

        List<PostStudy> postStudies = new ArrayList<>();
        for (Study study : studies) {
            postStudies.add(PostStudy.create(post1, study));
            postStudies.add(PostStudy.create(post2, study));
        }

        //when
        List<PostStudy> findPostStudies = postStudyRepository.findStudiesWhereInPosts(List.of(post1, post2));

        //then
        assertThat(findPostStudies).containsExactlyInAnyOrderElementsOf(postStudies);
    }
}