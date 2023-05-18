package study.querydsl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
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
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
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

    @Test
    void basicCase(){

        List<String> fetch = queryFactory.select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무 살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test // case문이 복잡할 떄 : CaseBuilder 사용!
    void complexCase(){


        List<String> result = queryFactory
                .select(new CaseBuilder() // 이 case문은 basicCase()의 문법으로는 안 된다.
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21살~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }


    }

    /**
     * 강사 왈 : CASE문은 애플리케이션 레벨에서 처리를 해라!!
     * -> DB로부터는 GROUPING, 최소한의 필터링으로 데이터를 들고 오고,
     * 그 데이터들을 이용하여 계산, 가공하는 것은 애플리케이션 레벨에서 하는 것이 옳다고 함
     * CASE문도 자기는 애플리케이션 레벨에서 코딩을 한다고 함
     *
     */

    @Test
    void constant(){

        List<Tuple> a = queryFactory
                .select(member.username, Expressions.constant("A")) // select의 projection에 무조건 상수 "A"를 집어 넣음.
                .from(member)
                .fetch();
        for (Tuple tuple : a) {
            System.out.println("tuple = " + tuple);
        }
    }


    @Test
    void concat(){

        List<String> results = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test // Projection 대상이 하나 : 반환 타입을 명확히 지정 가능
    void simpleProjection(){

        QMember m1 = new QMember("m1");

        //username의 타입은 String이다.
        List<String> results = queryFactory
                .select(m1.username)
                .from(m1)
                .fetch();
        for (String result : results) {
            System.out.println("result = " + result);
        }

    }


    @Test // Projection 대상이 2개 이상 : Tuple,Dto로 반환 받아야 한다.
    void tupleProjections(){

        QMember m1 = new QMember("m1");

        //Tuple로 받아야 한다.
        List<Tuple> results =
                queryFactory
                .select(m1.username,m1.age)
                .from(m1)
                .fetch();

        for (Tuple result : results) {

            String username = result.get(m1.username);
            System.out.println("username = " + username);
            int age = result.get(m1.age);
            System.out.println("age = " + age);


        }


    }

    @Test // Projection 대상이 2개 이상 : Tuple,Dto로 반환 받아야 한다.
    void findDtoByJPQL(){

        List<MemberDto> result = entityManager.createQuery(
                // new 연산자를 사용하여 Dto로 바로 변환 -> 지저분하다
                // => Querydsl은 이 문제를 해결!(Property 접근법, 필드 직접 접근, 생성자 사용)
                        "SELECT new study.querydsl.dto.MemberDto(m.username,m.age) FROM Member m"
                        , MemberDto.class
                )
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }

    }

    @Test // [Property 접근법] : getter를 사용하여 property에 접근하고, setter를 이용하여 값을 삽입
    void findDtoBySetter(){

        List<MemberDto> results = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto result : results) {
            System.out.println("result = " + result);
        }


    }

    @Test // [필드 직접 접근] : 이건 getter, setter 없이, 필드에 바로 값을 꽂음.
    void findDtoByField(){

        List<MemberDto> results = queryFactory
                .select(Projections.fields(MemberDto.class, // bean() -> fields()로 변경
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test // [생성자접근]
    void findDtoByConstructor(){

        List<MemberDto> results = queryFactory
                .select(Projections.constructor(MemberDto.class, // fields() -> constructor()로 변경
                        member.username, // MemberDto에서 정의한 생성자의 매개변수 순서에 맞게 값들을 세팅해야 한다.
                        member.age))    // MemberDto(String username,int age) -> member.username,member.age
                .from(member)           // -> 필드명까지 같을 필요 x.(생성자에 의한 Projection은 매개 변수의 [타입]만 일치하면 된다.)
                .fetch();               // 즉, member.[username], member.[age]이 아니여도,
                                        //예를 들어, 필드명을 다음과 같이 적어도도  member.name의 [타입]이 String, member.[aaa] 부분이 int형이면 ㄱㅊ.

        for (MemberDto result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test
    void findUserDto(){

        List<UserDto> results = queryFactory
                .select(Projections.fields(UserDto.class, // Dto가 MemberDto에서 UserDto로 바뀜.
                        member.username.as("name"), // UserDto의 필드에는 [username]이라는 필드명이 없다.(결과를 출력하면, name = [null]로 뜰 것이다.)
                        member.age))                    // 고로, as를 사용하여서 필드명을 매칭시켜 줘야 한다.
                .from(member)
                .fetch();

        for (UserDto result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test // 서브 쿼리에 alias를 설정하여, UserDto의 필드에 서브쿼리의 결과 값을 삽입 가능하다.
    void findUserDtoSubQuery(){

        QMember memberSub = new QMember("memberSub");


        List<UserDto> results = queryFactory
                .select(Projections.fields(UserDto.class,

                        member.username.as("name"),

                        //서브 쿼리의 결과를 UserDto의 "name"필드에 삽입
                        ExpressionUtils
                                .as(
                                JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub),"age") // 서브 쿼리의 결과값의 alias를 "age"로 줘서, UserDto::name에 삽입

                        ))
                .from(member)
                .fetch();

        for (UserDto result : results) {
            System.out.println("result = " + result);
        }
    }


    @Test // @QueryProjection을 사용한 Dto 전환!
    void findDtoByQueryProjection(){


        List<MemberDto> results = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        // Q.이 방식은 findDtoByConstructor()의 방식과 무엇이 다를까??
        // A. 이 방식은 예를 들어, 생성자의 매개변수로 member.username,member.age외에 실수로 member.teamname을 넣어도
        // 실행 해야지만 에러가 나는 [런타임]에러가 난다.
        // 그러나, 위 방식은 [컴파일 타임] 에러로 잡을 수가 있는 굉장한 장점이 있다.
        // 단점도 존재한다.
        // @QueryProjection은 Querydsl 라이브러리가 제공하는 기능이다.
        // -> 즉, Querydsl에 [의존적]이게 된다.
        // 만약, 도중에 querydsl을 사용하지 못하게 하면, DTO로 변환하는 코드를 Querydsl 없이 다시 새로 고쳐야 한다.


        for (MemberDto result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test // 동적 쿼리 해결법 1] BooleanBuilder 사용
    void dynamicQuery_BooleanBuilder(){

        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        // BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond)); // 반드시, usernameCond이 들어 오는 게 보장되는 경우, 이렇게 생성자에 직접 넣을 수도 있다.
        // 만약, username이 매개 변수로 들어 왔다면, 아래의 SELECTION CONDITION을 WHERE절에 삽입
        if(usernameCond != null){
            builder
                    .and(member.username.eq(usernameCond));
        }
        // 만약, age가 매개 변수로 들어 왔다면, 아래의 SELECTION CONDITION을 WHERE절에 삽입
        if(ageCond != null){
            builder
                    .and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)

                .where(  builder  ) // SELECTION CONDTION을 동적으로 생성한 것을 BooleanBuilder에 담아 넣음.
                //.where(  builder.and(member.username.eq("member1"))   ) // builder+[.and(), .or()]로 추가적인 조건을 넣을 수도 있다.
                // .where(  builder.or(member.username.eq("member1"))   )
                .fetch();


    }

    @Test // 동적 쿼리 해결법 2] Where 다중 파라미터 사용(BooleanBuilder보다 이 방식이 훨씬 코드 깔끔하고 좋다고 권장)
    void dynamic_WhereParam_0(){


        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2_0(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);



    }

    private List<Member> searchMember2_0(String usernameCond, Integer ageCond) {

         return queryFactory
                 .selectFrom(member)
                 .where(usernameEq(usernameCond),ageEq(ageCond))
                 //where(null,null)일 경우 아무런 selection condition이 안 들어 감
                 //where(null,member.age.eq(ageCond))이면, age에 대한 selection condition이 동적으로 만들어 짐.
                 .fetch();
    }
    private Predicate usernameEq(String usernameCond) {

        if(usernameCond != null)
            return member.username.eq(usernameCond);
        else
            return null;
    }

    private Predicate ageEq(Integer ageCond) {
        if(ageCond != null)
            return member.age.eq(ageCond);
        else
            return null;
    }

    void dynamic_WhereParam_1(){ // 이 방식을 더 선호!


        String usernameParam = "member1";
        Integer ageParam = 10;

        // 동적 쿼리르 메서드의 조합으로 만들 수가 있다(메서드를 재사용하여 여러 가지 동적 쿼리의 조합을 만들 수가 있다)
        List<Member> result = searchMember2_1(usernameParam,ageParam);

        assertThat(result.size()).isEqualTo(1);


    }

    private List<Member> searchMember2_1(String usernameCond, Integer ageCond) {

        return queryFactory
                .selectFrom(member)
                // where()의 매개변수는 Predicate로 받는데, BooleanExpression은 Predicate 인터페이스의 구현체이므로, 받을 수가 있다.
                .where(allEq(usernameCond,ageCond)) // 조립해서 사용 가능!
                .fetch();
    }

    // Predicate는 [인터페이스], BooleanExpression은 Predicate의 구현체이다.
    private BooleanExpression usernameEq_1(String usernameCond) {

        if(usernameCond != null)
            return member.username.eq(usernameCond);
        else
            return null;
    }

    private BooleanExpression ageEq_1(Integer ageCond) {

        if(ageCond != null)
            return member.age.eq(ageCond);
        else
            return null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {

        // 동적 쿼리를 조립할 수가 있는 장점이 있다.
        return usernameEq_1(usernameCond)
                .and(ageEq_1(ageCond));
    }


    @Test
    void bulkUpdate(){

        // bulk 연산 시, 주의 사항!!
        // -> execute()가 실행되면, context에 어떤 쿼리문이 저장돼 있든 무시하고 단독으로 db에 날라간다.
        // 이때 Context에 있는 쿼리문을 모두 flush 해주고 난 뒤에, execute()를 실행시켜 줘야
        // 데이터 불일치와 같은 문제가 안 생긴다.
        entityManager.flush();
        entityManager.clear(); // clear()를 하지 않으면, 벌크 연산이 반영되지 않은 [기존]의 데이터들이 캐싱돼서 온다.

        // update된 row 수 반환!
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
    }












}







