package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.PositioningMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositioningMapRepository extends JpaRepository<PositioningMap, Long> {
}