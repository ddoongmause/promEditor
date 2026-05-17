package com.ddoongddak.promeditor.template.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class TemplateCreateRequest {

    @NotBlank
    private String title;

    private String description;

    private String category;

    private List<String> tags = new ArrayList<>();

    @Valid
    private List<BlockRequest> blocks = new ArrayList<>();
}
