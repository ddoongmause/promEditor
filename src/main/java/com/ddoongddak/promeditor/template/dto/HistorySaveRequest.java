package com.ddoongddak.promeditor.template.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class HistorySaveRequest {

    /** {"슬롯이름": "입력값"} */
    @NotNull
    private Map<String, String> slotValues;
}
