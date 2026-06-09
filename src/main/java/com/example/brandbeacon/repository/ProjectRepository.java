package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.Project;
import com.example.brandbeacon.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
}