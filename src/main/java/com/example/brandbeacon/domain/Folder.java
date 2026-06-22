package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "FOLDER")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FOLDER_ID")
    private Long folderId; // 폴더 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Member member; // 이 폴더를 만든 주인(회원)

    @Column(name = "FOLDER_NAME", nullable = false, length = 100)
    private String folderName; // 폴더 이름

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt; // 생성 날짜

    public void updateFolderName(String newName) {
        this.folderName = newName;
    }
}
