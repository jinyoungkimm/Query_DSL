package study.querydsl.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository repository;
    private final MemberRepository memberRepository;
    @GetMapping("/v1/members")
    public List<MemberTeamDto>  searchMemberV1(MemberSearchCondition condition){
        return repository.search(condition);
    }




}
