package seong.onlinestudy.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import seong.onlinestudy.domain.Post;
import seong.onlinestudy.enumtype.PostCategory;

import java.util.List;

public interface PostRepositoryCustom {

    /**
     * Post 엔티티로부터 Comment, Member 엔티티를 페치 조인하고 조건에 따른 목록을 조회한다.
     *
     * @param memberId Post 엔티티의 회원 조건 검색을 위한 Member 의 id
     * @param groupId Post 엔티티가 소속된 그룹 조건을 위한 Group 의 id
     * @param search like '%search%' 조건을 위한 문자열
     * @param category category 검색을 위한 PostCategory enum
     * @param studyIds Study 와 연관관계 조건을 위한 id List
     * @param pageable page, offset 데이터를 이용
     * @return Comment, Member 와 페치 조인한 Post 목록을 반환
     * */
    Page<Post> findPostsWithComments(Long memberId, Long groupId, String search, PostCategory category, List<Long> studyIds, Pageable pageable);

}
