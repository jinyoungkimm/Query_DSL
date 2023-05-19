package study.querydsl.dto;


import lombok.Data;

@Data // 검색 조건(selection condition) : 이 조건들을 가지고 동적 쿼리를 만든다.
public class MemberSearchCondition {

    //회원명, 팀명, 나이(ageGoe,ageLoe)

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;



}
