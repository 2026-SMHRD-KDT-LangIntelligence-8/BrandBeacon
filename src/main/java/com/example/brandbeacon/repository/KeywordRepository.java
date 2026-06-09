package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}