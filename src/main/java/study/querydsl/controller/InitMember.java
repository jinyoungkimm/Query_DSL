package study.querydsl.controller;


import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@Profile("local") // 아래 빈은 test에서는 동작하지 않고, 애플리케이션이 로딩될 때에만 동작한다.
@Component
@RequiredArgsConstructor
public class InitMember {


    private final InitMemberService initMemberService;


    @PostConstruct
    public void init(){
        initMemberService.init();
    }

    //Q. 아래에서 클래스를 선언하지 말고, @PostConstruct 메서드 안에 샘플 데이터를 입력하면 되는 것이 아닌가?
    //A. 정확한 설명은 해주지 않았지만, @PostConstruct와 @Transactional의 라이프 사이클이 다르기에,
    // 이 2 에노테이션을 동시에 붙이지는 못한다고 한다.
    // 그래서 @PostConstruct와 Transaction이 동작하는 부분을 분리해줘야 한다고 한다.

    @Component
    static class InitMemberService{

        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for(int i = 0;i< 100;i++){

                Team selectedTeam = (i % 2) == 0 ? teamA:teamB;
                em.persist(new Member("member"+i,i,selectedTeam));
            }



        }



    }



}
