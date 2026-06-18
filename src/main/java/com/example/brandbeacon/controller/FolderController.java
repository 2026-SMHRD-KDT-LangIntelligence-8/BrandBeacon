package com.example.brandbeacon.controller;

import com.example.brandbeacon.domain.Folder;
import com.example.brandbeacon.domain.Member;
import com.example.brandbeacon.repository.FolderRepository;
import com.example.brandbeacon.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // 1. 내 폴더 목록 불러오기 API
    @GetMapping("/my")
    public ResponseEntity<?> getMyFolders(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId"); // 세션에서 로그인한 유저 확인
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

        // 새 폴더 객체 조립 후 DB에 저장!
        Folder folder = Folder.builder()
                .member(member)
                .folderName(folderName)
                .build();
        folderRepository.save(folder);

        // 숫자(ID)와 문자(메시지)가 섞여 있으므로 <String, Object> 명시!
        return ResponseEntity.ok(Map.<String, Object>of(
                "message", "폴더가 성공적으로 생성되었습니다.",
                "folderId", folder.getFolderId()
        ));
    }
}