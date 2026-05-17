package com.ddoongddak.promeditor.template.dto;

import com.ddoongddak.promeditor.template.entity.Template;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class TemplateSummaryResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final boolean isFavorite;
    private final List<String> tags;
    private final LocalDateTime updatedAt;

    public TemplateSummaryResponse(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.description = template.getDescription();
        this.category = template.getCategory();
        this.isFavorite = template.isFavorite();
        this.tags = template.getTags().stream().map(t -> t.getTag()).toList();
        this.updatedAt = template.getUpdatedAt();
    }
}
