package seong.onlinestudy.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StudyRepositoryTest {

    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostRepository postRepository;



}