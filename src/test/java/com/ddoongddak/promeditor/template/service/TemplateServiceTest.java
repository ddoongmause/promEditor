package com.ddoongddak.promeditor.template.service;

import com.ddoongddak.promeditor.template.dto.*;
import com.ddoongddak.promeditor.template.entity.*;
import com.ddoongddak.promeditor.template.repository.SlotOptionRepository;
import com.ddoongddak.promeditor.template.repository.TemplateBlockRepository;
import com.ddoongddak.promeditor.template.repository.TemplateRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateBlockRepository templateBlockRepository;

    @Mock
    private SlotOptionRepository slotOptionRepository;

    @InjectMocks
    private TemplateService templateService;

    // ── getTemplateFacade ──────────────────────────────

    @Test
    void getTemplateFacade_존재하는ID_템플릿반환() {
        // given
        Template template = Template.builder().title("테스트 템플릿").description("설명").build();
        given(templateRepository.findById(1L)).willReturn(Optional.of(template));

        // when
        TemplateResponse result = templateService.getTemplateFacade(1L);

        // then
        assertThat(result.getTitle()).isEqualTo("테스트 템플릿");
        verify(templateRepository).findById(1L);
    }

    @Test
    void getTemplateFacade_존재하지않는ID_예외발생() {
        // given
        given(templateRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> templateService.getTemplateFacade(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ── getTemplateListFacade ──────────────────────────

    @Test
    void getTemplateListFacade_템플릿존재_목록반환() {
        // given
        Template t1 = Template.builder().title("템플릿1").build();
        Template t2 = Template.builder().title("템플릿2").build();
        given(templateRepository.findAll()).willReturn(List.of(t1, t2));

        // when
        List<TemplateSummaryResponse> result = templateService.getTemplateListFacade();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("템플릿1");
    }

    @Test
    void getTemplateListFacade_템플릿없음_빈목록반환() {
        // given
        given(templateRepository.findAll()).willReturn(List.of());

        // when
        List<TemplateSummaryResponse> result = templateService.getTemplateListFacade();

        // then
        assertThat(result).isEmpty();
    }

    // ── createTemplateFacade ───────────────────────────

    @Test
    void createTemplateFacade_블록없음_템플릿저장() {
        // given
        TemplateCreateRequest request = new TemplateCreateRequest();
        setField(request, "title", "새 템플릿");
        setField(request, "description", "설명");
        setField(request, "blocks", List.of());

        Template saved = Template.builder().title("새 템플릿").description("설명").build();
        given(templateRepository.save(any())).willReturn(saved);

        // when
        TemplateResponse result = templateService.createTemplateFacade(request);

        // then
        assertThat(result.getTitle()).isEqualTo("새 템플릿");
        verify(templateRepository).save(any());
    }

    @Test
    void createTemplateFacade_슬롯블록포함_블록과옵션저장() {
        // given
        SlotOptionRequest optionRequest = new SlotOptionRequest();
        setField(optionRequest, "label", "한국어");

        BlockRequest blockRequest = new BlockRequest();
        setField(blockRequest, "blockType", BlockType.SLOT);
        setField(blockRequest, "content", "언어");
        setField(blockRequest, "slotType", SlotType.SELECT);
        setField(blockRequest, "options", List.of(optionRequest));

        TemplateCreateRequest request = new TemplateCreateRequest();
        setField(request, "title", "슬롯 템플릿");
        setField(request, "blocks", List.of(blockRequest));

        Template savedTemplate = Template.builder().title("슬롯 템플릿").build();
        TemplateBlock savedBlock = TemplateBlock.builder()
                .template(savedTemplate).blockType(BlockType.SLOT)
                .content("언어").slotType(SlotType.SELECT).sortOrder(0).build();

        given(templateRepository.save(any())).willReturn(savedTemplate);
        given(templateBlockRepository.save(any())).willReturn(savedBlock);
        given(slotOptionRepository.save(any())).willReturn(
                SlotOption.builder().block(savedBlock).label("한국어").sortOrder(0).build());

        // when
        TemplateResponse result = templateService.createTemplateFacade(request);

        // then
        assertThat(result.getTitle()).isEqualTo("슬롯 템플릿");
        verify(templateBlockRepository).save(any());
        verify(slotOptionRepository).save(any());
    }

    // ── deleteTemplateFacade ───────────────────────────

    @Test
    void deleteTemplateFacade_존재하는ID_삭제성공() {
        // given
        given(templateRepository.existsById(1L)).willReturn(true);

        // when
        templateService.deleteTemplateFacade(1L);

        // then
        verify(templateRepository).deleteById(1L);
    }

    @Test
    void deleteTemplateFacade_존재하지않는ID_예외발생() {
        // given
        given(templateRepository.existsById(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> templateService.deleteTemplateFacade(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ── 리플렉션 헬퍼 (NoArgsConstructor DTO 필드 세팅용) ──

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
