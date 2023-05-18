package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

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

    @Test
    public void paging1(){


        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc()) // 페이징을 확인할 떄에는, 보통 sorting을 넣는다. 왜냐하면, sorting을 해놔야 paging이
                // 잘 됐는지 확인할 수가 있다.
                .offset(1)
                .limit(2)
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
        assertThat(result.size()).isEqualTo(2);

    }

    @Test
    public void paging2(){

        // 전체 totalCount가 필요할 때!
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4); // 여기서 count() 쿼리 1번
        assertThat(result.getResults().size()).isEqualTo(2); // 여기서 결과 조회 쿼리 1번


        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);

    }

    @Test   // [집계 함수]
    void aggregation(){

        // [Tuple]은 Querydsl에서 제공하는 타입
        // => 반환 타입을 Q-Type으로 정해져 있는 게 아니다. 그럴 때에는 Querydsl이 Tuple형으로 받게 돼 있다.
        List<Tuple> results = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = results.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(40);
        assertThat(tuple.get(member.age.avg())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(10);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /**
     *
     * 팀의 이름과 각 팀의 평균 연령을 구해라 .
     */

    @Test
    void groupBy() throws Exception{


        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)  // JOIN 부분은 나중에 설명을 함.
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(10);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(10);


    }


    @Test
    void join(){

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1","member2");
    }

    /**
     * 세타 조인
     * -> 회원 이름이 팀 이름과 같은 회원을 조회!
     */

    @Test
    void theta_join_no_on(){ // [cross join] : 연관 관계가 설정돼 있지 않는 엔티티들끼리도 JOIN이 가능하다.

        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team) // 연관 관계가 없어도, cross join으로 join 가능!!
                .where(member.username.eq(team.name))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");

    }

    /**
     * THETA JOIN에는 한가지 제약이 있다.
     * -> THETA JOIN 시, Outer Join이 불가능하다.
     * 그러나, 최근의 하이버네이트에서는 join이 [on]을 사용하여, theta join을 하면서도 [on]을 사용하여 outer join 기능을 지원하였다.
     * ( 자세한 건 다음 시간에!!! )
     */


    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 [모두](=left outer join) 조회
     * JPQL: SELECT m,t from Member m left join m.team t [on] t.name = 'teamA'
     */

    @Test
    void join_on_filtering(){

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    /**
     * 세타 조인
     * -> 회원 이름이 팀 이름과 같은 회원을 left outer 조회!
     */

    @Test
    void theta_join_on(){ // [cross join] : 연관 관계가 설정돼 있지 않는 엔티티들끼리도 JOIN이 가능하다.

        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member,team)

                .from(member)
                .leftJoin(team) // == FROM [member left join team]
                // 원래 leftjoin()은 leftJoin(member,tema) 이런 식으로 적으면, PK,FK로 join(left join)을 한다.
                // 근데, leftjoin(team) 이렇게, 하나만 적게 되면 PK,FK로 Join하지 않고
                // 뒤에 오는 on 절에 의해서만 join이 된다
                // (하이버네이트5.1부터 연관 관계가 아닌 엔티티끼리 on을 이용하여 외부조인 가능해짐) : leftJoin(객체1개만).ON(~~)
                // (물론, 내부 조인도 가능)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    void fetchJoinNo(){

        //DB로부터 쿼리를 들고 오겠다.
        entityManager.flush();
        entityManager.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        // Team 엔티티가 초기화됬는지 안 됐는지 알려준다.(Team에는 Lazy가 걸려 있다)
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).isFalse();

    }

    @Test
    void fetchJoinYes(){

        //DB로부터 쿼리를 들고 오겠다.
        entityManager.flush();
        entityManager.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin() // fetch join
                .where(member.username.eq("member1"))
                .fetchOne();

        // Team 엔티티가 초기화됬는지 안 됐는지 알려준다.(Team에는 Lazy가 걸려 있다)
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).isTrue();

    }

    /**
     * 나이가 가장 많은 회원 조회!
     */

    @Test
    void subQuery1(){

        // 서브 쿼리에서의 QMember와 서브 쿼리 밖에서의 QMember의 alias는 겹치면 안 되님깐,
        // 서브 쿼리에서 사용할 QMember를 따로 생성!
        QMember memberSub = new QMember("memberSub");

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.eq(

                        //서브 쿼리
                        JPAExpressions.select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }


    }

    /**
     * 나이가 평균 이상인 회원!
     */

    @Test
    void subQuery2(){

        // 서브 쿼리에서의 QMember와 서브 쿼리 밖에서의 QMember의 alias는 겹치면 안 되님깐,
        // 서브 쿼리에서 사용할 QMember를 따로 생성!
        QMember memberSub = new QMember("memberSub");

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.goe(

                        //서브 쿼리
                        JPAExpressions.select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }

    }

    @Test
    void subQuery2_IN(){

        // 서브 쿼리에서의 QMember와 서브 쿼리 밖에서의 QMember의 alias는 겹치면 안 되님깐,
        // 서브 쿼리에서 사용할 QMember를 따로 생성!
        QMember memberSub = new QMember("memberSub");

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.in(

                        //서브 쿼리
                        JPAExpressions.select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }

    }

    @Test // where이 아닌, select안에서 서브 쿼리 사용!
    void subQuery3(){

        QMember memberSub = new QMember("memberSub");

        List<Tuple> fetch = queryFactory

                .select(member.username,

                        JPAExpressions.select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }


    }






}







