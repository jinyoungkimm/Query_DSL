package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager entityManager;

    JPAQueryFactory queryFactory; // 이 필드값에 여러 쓰레드가 접근을 하면 동시성(concurrency) 문제가 생기지 않는가?
                                  // -> 동시성 문제가 생기지 않도록 코딩이 돼 있다!!

    @BeforeEach
    void beforeEach(){

        queryFactory = new JPAQueryFactory(entityManager);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamA);

        Member member3 = new Member("member3", 10, teamB);
        Member member4 = new Member("member4", 10, teamB);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

    }


    @Test
    void startJPQL(){

        // member1을 찾아라!!!
        Member member1 = entityManager.createQuery("SELECT m FROM Member m" +
                        " WHERE m.username = :username",Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(member1.getUsername()).isEqualTo("member1");

    }

    @Test // Querydsl 사용법 1]
    void startQuerydslV1(){

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        QMember m = new QMember("m");

        // JPQL과는 다르게, java 코드로 작성을 했다. -> 컴파일 타임 때, 오류를 알려 줌!!!(이 부분이 Querydsl의 막강한 장점)
        Member member1 = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

         assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test// Querydsl 사용법 2]
    void startQuerydslV2(){

        //필드와, beforeEach()를 이용하여 아래 코드 부분을 대체할 수가 있다.
       //JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        QMember m = new QMember("m");

        // JPQL과는 다르게, java 코드로 작성을 했다. -> 컴파일 타임 때, 오류를 알려 줌!!!(이 부분이 Querydsl의 막강한 장점)
        Member member1 = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test  // Querydsl 사용법 3]
    void startQuerydslV3(){

        QMember m = member; // ==  QMember m = new QMember("mermber1")

        Member member1 = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test  // Querydsl 사용법 4] 가장 권장함
    void startQuerydslV4() {

        Member member1 = queryFactory
                .select(member) // static import로 더 깔끔히 가능!
                .from(member)
                .where(member.username.eq("member1")) //파라미터 바인딩 처리
                .fetchOne();

        assertThat(member1.getUsername()).isEqualTo("member1");


    }

    @Test
    public void seach(){

        Member member1 = queryFactory
                .selectFrom(member) // == .select(member).From(member)
                .where(member.username.eq("member1").and(member.age.eq(10))) // AND 연산자를 검색 조건에 활용
                .fetchOne();                                                        // 더 많은 쿼리 연산자가 있다. PPT 참조!
        assertThat(member1.getUsername()).isEqualTo("member1");
        assertThat(member1.getAge()).isEqualTo(10);

    }


    @Test // [ 결과 조회 ]
    public void resultFetch(){


    //  List<Member> fetch = queryFactory
    //          .selectFrom(member)
    //          .fetch(); // List<>를 반환하며, 결과과 없을 시, [빈 리스트]를 반환

    // Member fetchOne = queryFactory
    //         .selectFrom(member)
    //         .fetchOne(); // 단건 조회!

    // Member fetchFirst = queryFactory
    //         .selectFrom(member)
    //         .fetchFirst();// == .limit(1).fetchFirst()와 같다

    //QueryResults<Member> results =  queryFactory
    //        .selectFrom(member)
    //        .fetchResults();

    // // fetchResults()가 아닌, fetch() 사용을 권장하고 있다.
    // long total = results.getTotal(); // 이 시점에서 SQL문이 1번 실행됨.
    // List<Member> content = results.getResults(); // 이 시점에서 SQL문이 날라가서, 결과를 가져 온다.


        long total = queryFactory
                .selectFrom(member)
                .fetchCount(); // 이것도 향후, Querydsl에서 지원을 하지 않기로 함.

    }

    /**
     * 회원 [정렬] 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력(null last)
     *
     */

    @Test
    void sort(){

        //[정렬] 테스트를 위하여, 데이터 추가!
        entityManager.persist(new Member(null,100));
        entityManager.persist(new Member("member5",100));
        entityManager.persist(new Member("member6",100));

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(),
                        member.username.asc().nullsLast()) // 회원 이름이 없으면 마지막에 출력(null last)

                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member Null = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(Null.getUsername()).isNull();


    }

}

