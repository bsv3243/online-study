package seong.onlinestudy.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.GroupRole;
import seong.onlinestudy.enumtype.PostCategory;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.MyUtils.*;

@Slf4j
@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    PostRepository postRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
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
        members = createMembers(50, false);
        memberRepository.saveAll(members);

        groups = createGroups(members, 20, false);
        groupRepository.saveAll(groups);

        posts = createPosts(members, groups, 20, false);
        postRepository.saveAll(posts);

        studies = createStudies(10, false);
        studyRepository.saveAll(studies);
    }

    @Test
    void createPost() {
        Member member = memberRepository.findAll().get(0);

        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        postRepository.save(post);
    }

    @Test
    void getPost() {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);

        postRepository.save(post);

        //when
        Post findPost = postRepository.findByIdWithMemberAndGroup(post.getId()).get();

        //then
        assertThat(findPost).isEqualTo(post);
        assertThat(findPost.getMember()).isEqualTo(member);
    }

    @Test
    void getPost_withMemberAndGroup() {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        Group group = groups.get(0);
        post.setGroup(group);

        postRepository.save(post);

        //when
        Post findPost = postRepository.findByIdWithMemberAndGroup(post.getId()).get();

        //then
        assertThat(findPost).isEqualTo(post);
        assertThat(findPost.getMember()).isEqualTo(member);
        assertThat(findPost.getGroup()).isEqualTo(group);
    }

    @Test
    void getPostWithStudies_withoutStudies() {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        Group group = groups.get(0);
        post.setGroup(group);

        postRepository.save(post);

        //when
        Post findPost = postRepository.findByIdWithStudies(post.getId()).get();

        //then
        assertThat(findPost).isEqualTo(post);
    }

    @Test
    void getPostWithStudies() {
        //given
        Member member = memberRepository.findAll().get(0);
        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        Group group = groups.get(0);
        post.setGroup(group);

        postRepository.save(post);

        List<Study> studies = this.studies.subList(0, 5);
        for (Study study : studies) {
            PostStudy.create(post, study);
        }

        //when
        Post findPost = postRepository.findByIdWithStudies(post.getId()).get();

        //then
        assertThat(findPost).isEqualTo(post);
        List<Study> findStudies = findPost.getPostStudies().stream().map(PostStudy::getStudy).collect(Collectors.toList());
        assertThat(findStudies).containsExactlyInAnyOrderElementsOf(studies);
    }

    @Test
    void deletePost() {
        //given
        Post testPost = posts.get(0);

        //when
        testPost.delete();
        em.flush();
        em.clear();

        //then
        List<Post> findPosts = postRepository.findAll();
        Long testPostId = testPost.getId();

        assertThat(findPosts).anySatisfy(findPost -> {
            assertThat(findPost.isDeleted()).isTrue();
        });
    }

    @Test
    void deleteAllByMemberId() {
        //given
        Member testMember = members.get(0);

        Group testGroup = createGroup("group", 30, testMember);
        groupRepository.save(testGroup);

        List<Post> posts = createPosts(List.of(testMember), List.of(testGroup), 30, false);
        postRepository.saveAll(posts);

        assertThat(testMember.getPosts().size()).isGreaterThanOrEqualTo(30);

        //when
        postRepository.softDeleteAllByMemberId(testMember.getId());
        em.flush();
        em.clear();

        //then
        testMember = memberRepository.findById(testMember.getId()).get();
        assertThat(testMember.getPosts()).allSatisfy(findPost -> {
                    assertThat(findPost.isDeleted()).isTrue();
                }
        );
    }

    @Test
    void findPostWhichGroupRemoved() {
        //given
        Member testMember = members.get(0);
        Group testGroup = groups.get(0);

        List<Post> testPosts = createPosts(List.of(testMember), List.of(testGroup), 20, false);
        postRepository.saveAll(testPosts);

        em.flush();

        //when
        testGroup.delete();
        em.flush();
        em.clear();

        //then
        Post testPost = testPosts.get(0);

        testPost = postRepository.findById(testPost.getId()).get();

        assertThat(testPost.getGroup().getId()).isEqualTo(testGroup.getId());

    }
}
