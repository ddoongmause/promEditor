package com.ddoongddak.promeditor.template.dto;

import com.ddoongddak.promeditor.template.entity.SlotOption;
import lombok.Getter;

@Getter
public class SlotOptionResponse {

    private final Long id;
    private final String label;
    private final int sortOrder;

    public SlotOptionResponse(SlotOption option) {
        this.id = option.getId();
        this.label = option.getLabel();
        this.sortOrder = option.getSortOrder();
    }
}
