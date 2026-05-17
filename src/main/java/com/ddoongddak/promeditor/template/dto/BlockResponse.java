package com.ddoongddak.promeditor.template.dto;

import com.ddoongddak.promeditor.template.entity.BlockType;
import com.ddoongddak.promeditor.template.entity.SlotType;
import com.ddoongddak.promeditor.template.entity.TemplateBlock;
import lombok.Getter;

import java.util.List;

@Getter
public class BlockResponse {

    private final Long id;
    private final BlockType blockType;
    private final String content;
    private final SlotType slotType;
    private final int sortOrder;
    private final List<SlotOptionResponse> options;

    public BlockResponse(TemplateBlock block) {
        this.id = block.getId();
        this.blockType = block.getBlockType();
        this.content = block.getContent();
        this.slotType = block.getSlotType();
        this.sortOrder = block.getSortOrder();
        this.options = block.getOptions().stream()
                .map(SlotOptionResponse::new)
                .toList();
    }
}
