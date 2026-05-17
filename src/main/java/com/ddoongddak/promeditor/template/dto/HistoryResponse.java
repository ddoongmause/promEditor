package com.ddoongddak.promeditor.template.dto;

import com.ddoongddak.promeditor.template.entity.TemplateHistory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HistoryResponse {

    private final Long id;
    private final String slotValues;   // JSON 문자열 그대로 반환 (클라이언트에서 파싱)
    private final LocalDateTime createdAt;

    public HistoryResponse(TemplateHistory history) {
        this.id = history.getId();
        this.slotValues = history.getSlotValues();
        this.createdAt = history.getCreatedAt();
    }
}
