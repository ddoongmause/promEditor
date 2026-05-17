package com.ddoongddak.promeditor.template.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TEMPLATE_TAGS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private Template template;

    @Column(nullable = false, length = 50)
    private String tag;

    @Builder
    public TemplateTag(Template template, String tag) {
        this.template = template;
        this.tag = tag;
    }
}
