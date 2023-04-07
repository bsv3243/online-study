package seong.onlinestudy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.enumtype.PostCategory;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static seong.onlinestudy.MyUtils.*;
import static seong.onlinestudy.domain.QComment.comment;
import static seong.onlinestudy.domain.QPost.post;
import static seong.onlinestudy.domain.QPostStudy.postStudy;

@DataJpaTest
public class PostRepositoryCustomTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    StudyRepository studyRepository;

    List<Member> members;
    List<Group> groups;
    List<Post> posts;
    List<Study> studies;

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        members = createMembers(20, false);
        memberRepository.saveAll(members);

        groups = createGroups(members, 2, false);
        groupRepository.saveAll(groups);

        posts = createPosts(members, groups, 3, false);
        postRepository.saveAll(posts);

        studies = createStudies(5, false);
        studyRepository.saveAll(studies);

        for(int i=0; i<studies.size(); i++) {
            PostStudy.create(posts.get(i%posts.size()), studies.get(i));
        }
    }

    @Test
    void findPosts() {
        //given
        Member member = createMember("member", "member");
        memberRepository.save(member);

        Group group = createGroup("테스트그룹", 30, member);
        groupRepository.save(group);

        List<Post> postList = createPosts(List.of(member), List.of(group), 20, false);
        postRepository.saveAll(postList);

        Long groupId = group.getId();
        String search = null;
        PostCategory category = null;
        List<Long> studyIds = null;

        //when
        List<Post> posts = query
                .select(post)
                .from(post)
                .leftJoin(post.comments, comment).fetchJoin()
                .leftJoin(post.postStudies, postStudy)
                .where(groupIdEq(groupId), searchContains(search), categoryEq(category), studyIdIn(studyIds))
                .orderBy(post.createdAt.desc())
                .fetch();

        Long count = query
                .select(post.count())
                .from(post)
                .leftJoin(post.postStudies, postStudy)
                .where(groupIdEq(groupId), searchContains(search), categoryEq(category), studyIdIn(studyIds))
                .fetchOne();

        //then
        assertThat(posts).containsAll(postList);
    }

    @Test
    @DisplayName("findPosts_검색어 조건")
    void findPosts_검색어조건() {
        //given
        Member testMember = members.get(0);
        Group testGroup = groups.get(0);
        Post testPost = createPost("검색", "검색테스트", PostCategory.CHAT, testMember);
        Post testPost2 = createPost("검검색검", "검색테스트", PostCategory.INFO, testMember);
        Post testPostNotContain = createPost("테스트", "검색테스트", PostCategory.INFO, testMember);
        testPost.setGroup(testGroup);
        testPost2.setGroup(testGroup);
        testPostNotContain.setGroup(testGroup);

        postRepository.saveAll(List.of(testPost, testPost2, testPostNotContain));

        //when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> findPostsWithPage = postRepository.findPostsWithComments(null, null, "검색", null, null, pageRequest);

        //then
        List<Post> findPosts = findPostsWithPage.getContent();
        assertThat(findPosts.size()).isGreaterThan(0);

        assertThat(findPosts).allSatisfy(findPost -> {
            assertThat(findPost.getTitle()).contains("검색");
        });
    }

    @Test
    @DisplayName("findPosts_게시글, 댓글 삭제 대이터 혼합")
    void findPosts_삭제데이터혼합() {
        //given
        Member testMember = members.get(0);
        Post testPost = posts.get(0);
        List<Comment> testComments = createComments(List.of(testMember), posts, 10, false);

        //when
        testPost.delete();
        for (Comment testComment : testComments) {
            testComment.delete();
        }
        em.flush();
        em.clear();

        PageRequest pageRequest = PageRequest.of(0, 30);
        Page<Post> findPostsWithPage
                = postRepository.findPostsWithComments(null, null, null, null, null, pageRequest);

        //then
        List<Post> findPosts = findPostsWithPage.getContent();
        List<Long> findPostIds = findPosts.stream().map(Post::getId).collect(Collectors.toList());
        Long testPostId = testPost.getId();

        assertThat(findPostIds).doesNotContain(testPostId);
        assertThat(findPosts).allSatisfy(findPost -> {
            assertThat(findPost.getComments().size()).isEqualTo(0);
        });
    }

    @Test
    void findPostsWithComments_회원조건() {
        //given
        Member testMember = members.get(0);

        //when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> findPostsWithPage = postRepository.findPostsWithComments(
                testMember.getId(), null, null,
                null, null, pageRequest);

        //then
        List<Post> findPosts = findPostsWithPage.getContent();
        List<Post> testPosts = testMember.getPosts();

        assertThat(findPosts).containsExactlyInAnyOrderElementsOf(testPosts);
    }

    @Test
    void findPostsWithComments_그룹삭제() {
        //given
        Member testMember = members.get(0);
        Group testGroup = groups.get(0);

        List<Post> testPosts = createPosts(List.of(testMember), List.of(testGroup), 20, false);
        postRepository.saveAll(testPosts);

        testGroup.delete();
        em.flush();
        em.clear();

        //when
        Page<Post> postsWithComments = postRepository
                .findPostsWithComments(null, testGroup.getId(), null, null, null, PageRequest.of(0, 10));

        //then
        List<Post> findPosts = postsWithComments.getContent();
        Post findPost = findPosts.get(0);

        assertThat(findPost.getGroup().getId()).isEqualTo(testGroup.getId());
    }

    private BooleanExpression studyIdIn(List<Long> studyIds) {
        return studyIds != null ? postStudy.study.id.in(studyIds) : null;
    }

    private BooleanExpression categoryEq(PostCategory category) {
        return category != null ? post.category.eq(category) : null;
    }

    private BooleanExpression searchContains(String search) {
        return search != null ? post.title.contains(search) : null;
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return groupId != null ? post.group.id.eq(groupId) : null;
    }
}
