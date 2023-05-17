package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
//@Commit // @Rollback(false)랑 같음
class MemberTest {


    @PersistenceContext
    EntityManager entityManager;

    @Test

    void testEntity(){

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

        //DB로부터 데이터를 FETCH하기 위하여!!!
        entityManager.flush();
        entityManager.clear();

        List<Member> result = entityManager.createQuery("SELECT m FROM Member m", Member.class)
                .getResultList();

        for (Member member : result) {
            System.out.println("member = " + member);
        }


    }




}