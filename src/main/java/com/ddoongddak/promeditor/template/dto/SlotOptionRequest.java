package com.ddoongddak.promeditor.template.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SlotOptionRequest {

    @NotBlank
    private String label;
}
