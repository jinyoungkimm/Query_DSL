package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository repository;



    @Test
    void basicTest(){

        Member member = new Member("member1", 10);
        repository.save(member);

        Member findMember = repository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> results = repository.findAll();
        assertThat(results).containsExactly(member);


        List<Member> resultss = repository.findByUsername("member1");
        assertThat(resultss).containsExactly(member);




    }

    @Test
    void basicTest_Querydsl(){

        Member member = new Member("member1", 10);
        repository.save(member);


        List<Member> results = repository.findAll_Querydsl();
        assertThat(results).containsExactly(member);


        List<Member> resultss = repository.findByUsername_Querydsl("member1");
        assertThat(resultss).containsExactly(member);




    }


    @Test
    void searchTest(){

    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    em.persist(teamA);
    em.persist(teamB);
    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);
    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);
    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);


    // selection conditon 생성
    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    // 생성한 selection condition을 가지고, select!
    List<MemberTeamDto> result = repository.search(condition);

    assertThat(result).extracting("username").containsExactly("member4");}

}