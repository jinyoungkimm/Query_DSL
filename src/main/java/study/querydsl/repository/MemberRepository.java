package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;

import java.util.List;


//Spring Data Jpa와 Querydsl의 조합
public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom { // 인터페이스는 다중 상속 가능!

    // 쿼리 메서드 기능을 이용
    List<Member> findByUsername(String username); // == "select m from Member m Where username = :username"([정적] 쿼리)

    // Spring Data JPA가 제공하는 [사용자 정의 인터페이스] 기능을 이용하여, [동적] 쿼리 구현
    //->Spring Data JPA는 [정적] 쿼리만 지원하기에 , [동적] 쿼리를 사용하고 싶다면, 사용자 정의 인터페이스를 새로 만들어서
    // 구현체를 만들어 주는 수밖에 없다.


    //페이징 기능도 보통, Spring Data JPA로 많이 사용한다.(Page,Pageable...)
    // 페이징 기능을 Spring Data Jpa와 Querydsl의 조합으로 어떻게 구현하는 지 알아 보자(MemberRepositoryCustom 인터페이스 참조)
}
