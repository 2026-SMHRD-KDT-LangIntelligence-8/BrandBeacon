package com.example.brandbeacon.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "REFERENCE_BRAND")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BRAND_ID")
    private Long brandId;

    @Column(name = "BRAND_NAME", nullable = false, unique = true, length = 100)
    private String brandName;

    @Column(name = "BRAND_X", nullable = false)
    private Float brandX;

    @Column(name = "BRAND_Y", nullable = false)
    private Float brandY;
}