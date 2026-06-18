package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    // 특정 회원이 만든 폴더 목록만 싹 다 가져오는 메서드
    List<Folder> findByMember_Id(Long userId);
}
