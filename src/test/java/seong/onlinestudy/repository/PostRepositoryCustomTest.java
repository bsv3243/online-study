package seong.onlinestudy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.domain.*;

import javax.persistence.EntityManager;

import java.util.List;

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

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);

        List<Member> members = createMembers(50, false);
        memberRepository.saveAll(members);

        List<Group> groups = createGroups(members, 10, false);
        groupRepository.saveAll(groups);

        List<Post> posts = createPosts(members, groups, 10, false);
        postRepository.saveAll(posts);

        List<Study> studies = createStudies(20, false);
        studyRepository.saveAll(studies);

        for(int i=0; i<20; i++) {
            PostStudy.create(posts.get(i%10), studies.get(i));
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
