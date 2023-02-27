package seong.onlinestudy.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostStudyRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    PostStudyRepository postStudyRepository;

    @Test
    void findStudiesWherePost() {
        //given
        Member member = MyUtils.createMember("test1234", "test1234");
        memberRepository.save(member);

        Post post = MyUtils.createPost("test", "test", PostCategory.CHAT, member);
        postRepository.save(post);

        List<Study> studies = MyUtils.createStudies(5);
        studyRepository.saveAll(studies);

        List<PostStudy> postStudies = MyUtils.createPostStudies(post, studies);

        //when
        List<PostStudy> findPostStudies = postStudyRepository.findStudiesWherePost(post);

        //then
        assertThat(findPostStudies).containsAll(postStudies);
    }
}