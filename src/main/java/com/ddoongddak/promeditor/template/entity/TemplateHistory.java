package com.ddoongddak.promeditor.template.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TEMPLATE_HISTORIES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private Template template;

    /** {"슬롯이름": "입력값"} 형태의 JSON 문자열 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String slotValues;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TemplateHistory(Template template, String slotValues) {
        this.template = template;
        this.slotValues = slotValues;
        this.createdAt = LocalDateTime.now();
    }
}
