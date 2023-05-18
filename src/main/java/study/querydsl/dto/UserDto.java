package study.querydsl.dto;


import lombok.Data;

@Data
public class UserDto {

    private String name;
    private int age;

    //Querydsl도 기본 생성자가 꼭 필요하다.
    public UserDto(){

    }

    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
