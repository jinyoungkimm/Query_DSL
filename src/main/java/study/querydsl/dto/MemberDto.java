package study.querydsl.dto;


import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 기본 생성자 자동 생성해줌.
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection // 이 에노테이션을 사용하면, 자동으로 QMemberDto가 만들어짐.
    public MemberDto(String username,int age){
        this.username = username;
        this.age = age;
    }


    /*public MemberDto(String username,int age){

        this.username = username;
        this.age = age;

    }*/



}
