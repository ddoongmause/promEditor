package com.ddoongddak.promeditor.template.dto;

import com.ddoongddak.promeditor.template.entity.BlockType;
import com.ddoongddak.promeditor.template.entity.SlotType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class BlockRequest {

    @NotNull
    private BlockType blockType;

    private String content;

    private SlotType slotType;

    private List<SlotOptionRequest> options = new ArrayList<>();
}
