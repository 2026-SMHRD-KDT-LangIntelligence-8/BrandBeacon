package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    // Keyword 엔티티들을 일괄 조회하는 메서드
    List<Keyword> findByKeywordNameIn(List<String> keywordNames);
}