package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "POSITIONING_MAP")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositioningMap {
    @Id
    @Column(name = "PROJECT_ID")
    private Long projectId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @Column(name = "CURRENT_X", nullable = false)
    private Float currentX;

    @Column(name = "CURRENT_Y", nullable = false)
    private Float currentY;
}