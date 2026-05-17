package com.ddoongddak.promeditor.template.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TEMPLATE_BLOCKS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private Template template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BlockType blockType;

    /** FIXED: 고정 텍스트 내용 / SLOT: 슬롯 이름 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** SLOT 타입일 때만 사용 */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private SlotType slotType;

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<SlotOption> options = new ArrayList<>();

    @Builder
    public TemplateBlock(Template template, BlockType blockType, String content,
                         SlotType slotType, int sortOrder) {
        this.template = template;
        this.blockType = blockType;
        this.content = content;
        this.slotType = slotType;
        this.sortOrder = sortOrder;
    }
}
