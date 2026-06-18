package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 마이페이지 출력을 위해 회원 ID로 프로젝트 목록을 찾는 기능 추가
    List<Project> findByMember_Id(Long memberId);

    // 나중에 폴더 안에 있는 프로젝트들만 쏙 필터링해서 꺼내오는 기능
    List<Project> findByFolder_FolderId(Long folderId);
}