package study.querydsl;


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




}

