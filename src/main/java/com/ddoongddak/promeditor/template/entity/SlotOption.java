package com.ddoongddak.promeditor.template.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SLOT_OPTIONS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlotOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BLOCK_ID", nullable = false)
    private TemplateBlock block;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    public SlotOption(TemplateBlock block, String label, int sortOrder) {
        this.block = block;
        this.label = label;
        this.sortOrder = sortOrder;
    }
}
