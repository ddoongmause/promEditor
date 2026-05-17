package com.ddoongddak.promeditor.template.service;

import com.ddoongddak.promeditor.template.dto.HistorySaveRequest;
import com.ddoongddak.promeditor.template.dto.HistoryResponse;
import com.ddoongddak.promeditor.template.dto.TemplateResponse;
import com.ddoongddak.promeditor.template.entity.Template;
import com.ddoongddak.promeditor.template.entity.TemplateHistory;
import com.ddoongddak.promeditor.template.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TemplateServicePhase5Test {

    @Mock private TemplateRepository templateRepository;
    @Mock private TemplateBlockRepository templateBlockRepository;
    @Mock private SlotOptionRepository slotOptionRepository;
    @Mock private TemplateTagRepository templateTagRepository;
    @Mock private TemplateHistoryRepository templateHistoryRepository;

    @InjectMocks
    private TemplateService templateService;

    // ── cloneTemplateFacade ───────────────────────────

    @Test
    void cloneTemplateFacade_존재하는ID_복제본생성() {
        // given
        Template original = Template.builder().title("원본").description("설명").category("개발").build();
        Template cloned  = Template.builder().title("원본 (복사본)").description("설명").category("개발").build();

        given(templateRepository.findById(1L)).willReturn(Optional.of(original));
        given(templateRepository.save(any())).willReturn(cloned);
        given(templateRepository.findById(cloned.getId())).willReturn(Optional.of(cloned));

        // when
        TemplateResponse result = templateService.cloneTemplateFacade(1L);

        // then
        assertThat(result.getTitle()).isEqualTo("원본 (복사본)");
        verify(templateRepository).save(any());
    }

    // ── saveHistoryFacade ─────────────────────────────

    @Test
    void saveHistoryFacade_유효한요청_히스토리저장() {
        // given
        Template template = Template.builder().title("템플릿").build();
        given(templateRepository.findById(1L)).willReturn(Optional.of(template));
        given(templateHistoryRepository.save(any())).willReturn(
                TemplateHistory.builder().template(template).slotValues("{}").build());

        HistorySaveRequest request = new HistorySaveRequest();
        setField(request, "slotValues", Map.of("언어", "한국어"));

        // when
        templateService.saveHistoryFacade(1L, request);

        // then
        verify(templateHistoryRepository).save(any());
    }

    // ── getHistoryFacade ──────────────────────────────

    @Test
    void getHistoryFacade_히스토리존재_최근5개반환() {
        // given
        Template template = Template.builder().title("템플릿").build();
        TemplateHistory h1 = TemplateHistory.builder().template(template).slotValues("{\"언어\":\"한국어\"}").build();
        TemplateHistory h2 = TemplateHistory.builder().template(template).slotValues("{\"언어\":\"영어\"}").build();

        given(templateHistoryRepository.findByTemplateIdOrderByCreatedAtDesc(
                any(), any(PageRequest.class))).willReturn(List.of(h1, h2));

        // when
        List<HistoryResponse> result = templateService.getHistoryFacade(1L);

        // then
        assertThat(result).hasSize(2);
    }

    // ── 리플렉션 헬퍼 ─────────────────────────────────

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
