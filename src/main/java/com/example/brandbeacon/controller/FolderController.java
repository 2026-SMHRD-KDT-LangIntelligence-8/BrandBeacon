package com.example.brandbeacon.controller;

import com.example.brandbeacon.domain.Folder;
import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.repository.FolderRepository;
import com.example.brandbeacon.repository.MemberRepository;
import com.example.brandbeacon.repository.ProjectRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderRepository folderRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    // 1. 내 폴더 목록 불러오기 API
    @GetMapping("/my")
    public ResponseEntity<?> getMyFolders(HttpSession session) {
        Long userId = (Long) session.getAttribute("loginUserId");
        if (userId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        List<Map<String, Object>> result = folderRepository.findByMember_Id(userId).stream()
                .map(f -> Map.<String, Object>of(
                        "folderId", f.getFolderId(),
                        "folderName", f.getFolderName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 2. 새 폴더 생성하기 API
    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("loginUserId");
            if (userId == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            String folderName = body.get("folderName");
            if (folderName == null || folderName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("폴더 이름을 입력해주세요.");
            }

            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Folder folder = Folder.builder()
                    .member(member)
                    .folderName(folderName)
                    .build();
            folderRepository.save(folder);

            return ResponseEntity.ok(Map.<String, Object>of(
                    "message", "폴더가 성공적으로 생성되었습니다.",
                    "folderId", folder.getFolderId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("폴더 생성 중 오류: " + e.getMessage());
        }
    }

    // 3. 폴더명 수정 API
    @Transactional
    @PatchMapping("/{folderId}")
    public ResponseEntity<?> renameFolder(@PathVariable Long folderId,
                                          @RequestBody Map<String, String> body,
                                          HttpSession session) {
        Long userId = (Long) session.getAttribute("loginUserId");
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        String newName = body.get("folderName");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("폴더 이름을 입력해주세요.");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        if (!folder.getMember().getId().equals(userId)) {
            return ResponseEntity.status(403).body("본인 폴더만 수정할 수 있습니다.");
        }

        folder.updateFolderName(newName.trim());
        return ResponseEntity.ok("폴더명이 수정되었습니다.");
    }

    // 4. 폴더 삭제 API
    @Transactional
    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable Long folderId, HttpSession session) {
        Long userId = (Long) session.getAttribute("loginUserId");
        if (userId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));

        if (!folder.getMember().getId().equals(userId)) {
            return ResponseEntity.status(403).body("본인 폴더만 삭제할 수 있습니다.");
        }

        // 폴더 소속 프로젝트의 folder 참조를 null로 초기화 후 폴더 삭제
        projectRepository.detachProjectsFromFolder(folderId);
        folderRepository.delete(folder);

        return ResponseEntity.ok("폴더가 삭제되었습니다.");
    }
}