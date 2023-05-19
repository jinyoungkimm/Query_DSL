package study.querydsl.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isEmpty;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {


    private final JPAQueryFactory queryFactory;

    private final EntityManager entityManager;

    public MemberJpaRepository(EntityManager em){
        this.entityManager = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member){
        entityManager.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member findMember = entityManager.find(Member.class, id);
        return Optional.of(findMember);
    }

    public List<Member> findAll(){

       return entityManager.createQuery("select m from Member m",Member.class)
                .getResultList();

    }

    public List<Member> findAll_Querydsl(){

        return queryFactory
                .select(member)
                .from(member)
                .fetch();

    }

    public List<Member> findByUsername(String username){

        return entityManager.createQuery("select m from Member m Where m.username = :username",Member.class)
                .setParameter("username",username)
                .getResultList();

    }

    public List<Member> findByUsername_Querydsl(String username){

        return queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq(username))
                .fetch();


    }
    // Builder 사용하여 동적 쿼리 구현
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){


        BooleanBuilder builder = new BooleanBuilder();

        //select condtion인 MemberSearchCondition을 가지고, 동적 쿼리를 만든다.
        if(StringUtils.hasText(condition.getUsername())){
             builder.and(member.username.eq(condition.getUsername()));
         }

        if(StringUtils.hasText(condition.getTeamName())){
             builder.and(team.name.eq(condition.getTeamName()));
         }
        if(condition.getAgeGoe() != null){
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if(condition.getAgeGoe() != null){
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("member_id"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder) // 동적 쿼리!
                .fetch();

    }

    // Where절에 다중 파라미터로 동적 쿼리 구현
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
