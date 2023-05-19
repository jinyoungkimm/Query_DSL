package study.querydsl.repository;


import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

//사용자 정의 인터페이스 with Spring Data JPA
public interface MemberRepositoryCustom {

    // select condition에 따른 [동적] 쿼리 구현
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex1(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex2(MemberSearchCondition condition, Pageable pageable);

}
