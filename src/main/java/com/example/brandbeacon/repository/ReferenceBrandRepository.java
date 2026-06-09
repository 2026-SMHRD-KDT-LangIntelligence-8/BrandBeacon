package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.ReferenceBrand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceBrandRepository extends JpaRepository<ReferenceBrand, Long> {
}