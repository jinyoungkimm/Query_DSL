package study.querydsl.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter@Setter
@ToString(of={"id","username","age"}) // 여기에다가 "team"을 넣으면, 무한루프 일어남!!!(모르겠으면, 게시물 참조)
public class Member {                 // 연관 관계 필드들은 안 넣어야 한다.

    @Id@GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private int age;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username){

        this.username=username;
        this.age =0;
        this.team = null;

    }


    public Member(String username,int age){

        this.username=username;
        this.age = age;
        this.team = null;

    }


    public Member(String username,int age, Team team){
        this.username=username;
        this.age = age;

        if(team != null){
            changeTeam(team);
        }

    }

    // 양뱡향 관계 편의 메서드
    public void changeTeam(Team team) {

        this.team = team;
        team.getMembers().add(this);

    }


}
