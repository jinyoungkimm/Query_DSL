package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static org.apache.logging.log4j.util.Strings.isEmpty;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    //생성자가 1개일 떈, 자동으로 @Autowired가 붙는다
    public MemberRepositoryImpl(EntityManager entityManager){
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    // Where절에 다중 파라미터로 [동적] 쿼리 구현
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {

        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset()) // [1번] 조회해 올 때, 몇 번째 부터 들고 올거냐?
                .limit(pageable.getPageSize()) // [1번] 조회해 올 떄, 몇 개를 들고 올거냐??
                .fetchResults(); // fetchResults() : [데이터 내용],[전체 카운트] 조희를 한다.

        // 아래 코드에서 총 2번의 쿼리문이 날라감.
        // 1번 : [데이터 내용] 조회
        // 2번 : [전체 카운트] 조회
        List<MemberTeamDto> content = results.getResults(); // [데이터 내용]용 쿼리 1번이 날라감.
        long total = results.getTotal(); // [전체 카운트] 계산용 sql문이 1번 날라감.

        return new PageImpl<>(content,pageable,total);

    }

    @Override
    public Page<MemberTeamDto> searchPageComplex1(MemberSearchCondition condition, Pageable pageable) {


        List<MemberTeamDto> contents = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();// fetchResult() -> fetch()로 변경 : [데이터 내용] 용 쿼리와 [전체 카운트] 용 쿼리를 분리
                // 위 코드는 [데이터 내용] 용 쿼리이다.

            long total  = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetchCount(); // fetchCount() : [전체 카운트]를 조회
        // 위 코드는 [전체 카운트]를 조회하는 쿼리

/**
 * [전체 카운트]를 분리하면 좋은 이유 : fetchResult()는 알아서 left join을 이용해서 totalcount 쿼리를 날린다.
 * -> 그러나, 상황에 따라서는 left join 같은 join을 하나도 안 이용하고 들고 올 수가 있는 경우가 있다.
 * 즉, 최적화를 해서 [전체 카운트]를 들고 올 수가 있다.
 * 그러나 fetchReulst()는 꼭 left join 등을 붙여서 쿼리를 날리기에 [최적화]가 불가능하다.
 * -> [전체 카운트] 쿼리를 최적화 하고 싶을 때에는, 이렇게 분리하면 된다.
 */

    return new PageImpl<>(contents,pageable,total);
    }

    @Override // [전체 카운트] 쿼리를 날리지 않아도, [전체 카운트]를 알 수 있는 경우가 있다.
    public Page<MemberTeamDto> searchPageComplex2(MemberSearchCondition condition, Pageable pageable) {

        List<MemberTeamDto> contents = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()));
        // fetchCount()를 하기 전에는 위 코드에서 [전체 카운트] 용 쿼리가 날라 가지 않는다.

        /**
         * 만약에 1개의 page의 size가 100개라고 하자. 근데, content의 size는 3개이다.
         * 그러면, 굳이 [전체 카운트] 쿼리를 날리지 않아도, content.size()를 통해서 [전체 카운트]를 알 수가 있다.
         * -> 페이지의 size()가 content의 size()보다 클 때는 쿼리를 날리지 않고,
         * 작을 때는 쿼리를 날리게 하는 방법이 아래와 같다.
         */

        return PageableExecutionUtils.getPage(contents,pageable,()-> countQuery.fetchCount());
    }

    private BooleanExpression ageLoe(Integer ageLoe) {

        return ageLoe == null ? null : member.age.loe(ageLoe);

    }

    private BooleanExpression ageGoe(Integer ageGoe) {

        return ageGoe == null ? null : member.age.goe(ageGoe);


    }



    private BooleanExpression teamNameEq(String teamName) {

        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }




    private BooleanExpression usernameEq(String username) {

        return isEmpty(username) ? null : member.username.eq(username);
    }
}
