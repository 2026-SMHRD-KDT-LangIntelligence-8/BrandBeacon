package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "PROJECT_KEYWORD")
@IdClass(ProjectKeyword.ProjectKeywordId.class) // 클래스 이름 변경 반영
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectKeyword {

    @Id // 이제 JPA 어노테이션으로 명확하게 인식됩니다.
    @ManyToOne
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @Id
    @ManyToOne
    @JoinColumn(name = "KEYWORD_ID")
    private Keyword keyword;

    // 내부 클래스 이름을 Id -> ProjectKeywordId로 변경
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectKeywordId implements Serializable {
        private Long project;
        private Long keyword;
    }
}