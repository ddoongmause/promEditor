package com.ddoongddak.promeditor.template.service;

import com.ddoongddak.promeditor.template.dto.TemplateSummaryResponse;
import com.ddoongddak.promeditor.template.entity.Template;
import com.ddoongddak.promeditor.template.repository.TemplateRepository;
import com.ddoongddak.promeditor.template.repository.TemplateBlockRepository;
import com.ddoongddak.promeditor.template.repository.TemplateTagRepository;
import com.ddoongddak.promeditor.template.repository.SlotOptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TemplateServicePhase4Test {

    @Mock private TemplateRepository templateRepository;
    @Mock private TemplateBlockRepository templateBlockRepository;
    @Mock private SlotOptionRepository slotOptionRepository;
    @Mock private TemplateTagRepository templateTagRepository;

    @InjectMocks
    private TemplateService templateService;

    // ── searchTemplatesFacade ─────────────────────────

    @Test
    void searchTemplatesFacade_키워드검색_일치항목반환() {
        // given
        Template t = Template.builder().title("코드 리뷰 요청").category("개발").build();
        given(templateRepository.search("코드", null)).willReturn(List.of(t));

        // when
        List<TemplateSummaryResponse> result = templateService.searchTemplatesFacade("코드", null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("코드 리뷰 요청");
    }

    @Test
    void searchTemplatesFacade_카테고리필터_해당카테고리반환() {
        // given
        Template t = Template.builder().title("글쓰기 템플릿").category("글쓰기").build();
        given(templateRepository.search(null, "글쓰기")).willReturn(List.of(t));

        // when
        List<TemplateSummaryResponse> result = templateService.searchTemplatesFacade(null, "글쓰기");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("글쓰기");
    }

    @Test
    void searchTemplatesFacade_결과없음_빈목록반환() {
        // given
        given(templateRepository.search("없는키워드", null)).willReturn(List.of());

        // when
        List<TemplateSummaryResponse> result = templateService.searchTemplatesFacade("없는키워드", null);

        // then
        assertThat(result).isEmpty();
    }

    // ── getFavoritesFacade ────────────────────────────

    @Test
    void getFavoritesFacade_즐겨찾기존재_목록반환() {
        // given
        Template t = Template.builder().title("즐겨찾기 템플릿").build();
        given(templateRepository.findByIsFavoriteTrue()).willReturn(List.of(t));

        // when
        List<TemplateSummaryResponse> result = templateService.getFavoritesFacade();

        // then
        assertThat(result).hasSize(1);
    }

    // ── toggleFavoriteFacade ──────────────────────────

    @Test
    void toggleFavoriteFacade_존재하는ID_즐겨찾기토글() {
        // given
        Template t = Template.builder().title("토글 템플릿").build();
        given(templateRepository.findById(1L)).willReturn(Optional.of(t));

        // when
        templateService.toggleFavoriteFacade(1L);

        // then
        verify(templateRepository).findById(1L);
    }

    @Test
    void toggleFavoriteFacade_존재하지않는ID_예외발생() {
        // given
        given(templateRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> templateService.toggleFavoriteFacade(999L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
