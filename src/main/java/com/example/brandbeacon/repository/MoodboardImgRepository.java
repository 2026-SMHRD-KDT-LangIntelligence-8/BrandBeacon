package com.example.brandbeacon.repository;

import com.example.brandbeacon.domain.MoodboardImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoodboardImgRepository extends JpaRepository<MoodboardImg, Long> {

    // 특정 프로젝트 번호로 저장된 이미지들을 모두 찾아오는 기능
    List<MoodboardImg> findByProject_ProjectId(Long projectId);

    // 특정 프로젝트에 속한 이미지 전체 삭제
    void deleteByProject_ProjectId(Long projectId);
}