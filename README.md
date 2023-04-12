# 나의 공부방
https://github.com/hseong3243/online-study_old <br/>
본 프로젝트는 위 링크 저장소의 MSA로 개발한 과거 버전을 모놀리틱의 형태로 처음부터 다시 개발중인 버전입니다. <br/>
프론트엔드 저장소는 다음 링크를 참조해주세요. <br/>
https://github.com/hseong3243/online-study-front

## API 문서
https://studybread.shop/docs/index.html

## 기술 스택
+ Frontend
  + Vue.js
+ Backend
  + Java
  + Spring MVC
  + Spring Data JPA
  + Spring Rest Docs

## DB
![온라인스터디DB(수정)](https://user-images.githubusercontent.com/48748265/231474247-4c115009-a513-4981-9cc7-db57eb3d085e.png)

+ Ticket, StudyTicket, RestTicket 3개의 테이블이 있었으나, 조인횟수를 줄이기 위해 JPA의 단일 테이블 전략을 사용하며 Ticket 테이블의 ticket_status 컬럼에 의해 테이블이 구분됩니다.
