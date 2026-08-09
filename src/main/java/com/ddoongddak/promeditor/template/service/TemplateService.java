package com.ddoongddak.promeditor.template.service;

import com.ddoongddak.promeditor.template.dto.*;
import com.ddoongddak.promeditor.template.entity.*;
import com.ddoongddak.promeditor.template.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateBlockRepository templateBlockRepository;
    private final SlotOptionRepository slotOptionRepository;
    private final TemplateTagRepository templateTagRepository;
    private final TemplateHistoryRepository templateHistoryRepository;
    private final ObjectMapper objectMapper;

    // ════════════════════════════════════════════════════════════
    // Facade 메서드 (공개 진입점 — for/if 제어문만 사용)
    // ════════════════════════════════════════════════════════════

    /** 템플릿 단건 조회 */
    public TemplateResponse getTemplateFacade(Long templateId) {
        // 1. 템플릿 조회
        Template template = this.findTemplate(templateId);

        // 2. 응답 변환
        return new TemplateResponse(template);
    }

    /** 템플릿 목록 조회 */
    public List<TemplateSummaryResponse> getTemplateListFacade() {
        // 1. 전체 목록 조회
        List<Template> templates = this.findAllTemplates();

        // 2. 응답 변환
        List<TemplateSummaryResponse> result = new ArrayList<>();
        for (Template template : templates) {
            result.add(new TemplateSummaryResponse(template));
        }
        return result;
    }

    /** 템플릿 생성 (블록 + 태그 + 슬롯 옵션 포함) */
    @Transactional
    public TemplateResponse createTemplateFacade(TemplateCreateRequest request) {
        // 1. 템플릿 저장
        Template template = this.saveTemplate(request);

        // 2. 태그 저장
        for (String tag : request.getTags()) {
            this.saveTag(template, tag);
        }

        // 3. 블록 저장
        int sortOrder = 0;
        for (BlockRequest blockRequest : request.getBlocks()) {
            TemplateBlock block = this.saveBlock(template, blockRequest, sortOrder++);

            // 4. SELECT 타입이면 옵션 저장
            if (block.getBlockType() == BlockType.SLOT && block.getSlotType() == SlotType.SELECT) {
                int optionOrder = 0;
                for (SlotOptionRequest optionRequest : blockRequest.getOptions()) {
                    this.saveSlotOption(block, optionRequest, optionOrder++);
                }
            }
        }

        // 5. 재조회 후 반환
        Template saved = this.findTemplate(template.getId());
        return new TemplateResponse(saved);
    }

    /** 템플릿 수정 (블록/태그 전체 교체) */
    @Transactional
    public TemplateResponse updateTemplateFacade(Long templateId, TemplateCreateRequest request) {
        // 1. 템플릿 조회
        Template template = this.findTemplate(templateId);

        // 2. 기본 정보 수정
        this.updateTemplateInfo(template, request);

        // 3. 태그 전체 교체
        template.clearTags();
        for (String tag : request.getTags()) {
            this.saveTag(template, tag);
        }

        // 4. 블록 전체 교체
        template.clearBlocks();
        int sortOrder = 0;
        for (BlockRequest blockRequest : request.getBlocks()) {
            TemplateBlock block = this.saveBlock(template, blockRequest, sortOrder++);

            if (block.getBlockType() == BlockType.SLOT && block.getSlotType() == SlotType.SELECT) {
                int optionOrder = 0;
                for (SlotOptionRequest optionRequest : blockRequest.getOptions()) {
                    this.saveSlotOption(block, optionRequest, optionOrder++);
                }
            }
        }

        // 5. 재조회 후 반환
        Template updated = this.findTemplate(templateId);
        return new TemplateResponse(updated);
    }

    /** 템플릿 삭제 */
    @Transactional
    public void deleteTemplateFacade(Long templateId) {
        // 1. 존재 여부 확인
        this.checkExists(templateId);

        // 2. 삭제 (CASCADE로 블록/태그/옵션 함께 삭제)
        templateRepository.deleteById(templateId);
    }

    /** 템플릿 검색 (제목/태그 키워드 + 카테고리 필터) */
    public List<TemplateSummaryResponse> searchTemplatesFacade(String keyword, String category) {
        // 1. 검색 실행
        List<Template> templates = this.searchTemplates(keyword, category);

        // 2. 응답 변환
        List<TemplateSummaryResponse> result = new ArrayList<>();
        for (Template template : templates) {
            result.add(new TemplateSummaryResponse(template));
        }
        return result;
    }

    /** 즐겨찾기 목록 조회 */
    public List<TemplateSummaryResponse> getFavoritesFacade() {
        // 1. 즐겨찾기 조회
        List<Template> favorites = this.findFavorites();

        // 2. 응답 변환
        List<TemplateSummaryResponse> result = new ArrayList<>();
        for (Template template : favorites) {
            result.add(new TemplateSummaryResponse(template));
        }
        return result;
    }

    /** 즐겨찾기 토글 */
    @Transactional
    public TemplateResponse toggleFavoriteFacade(Long templateId) {
        // 1. 템플릿 조회
        Template template = this.findTemplate(templateId);

        // 2. 즐겨찾기 토글
        this.toggleFavorite(template);

        // 3. 반환
        return new TemplateResponse(template);
    }

    // ════════════════════════════════════════════════════════════
    // Private 메서드 (세부 구현 — 모던 Java 스타일)
    // ════════════════════════════════════════════════════════════

    /** 템플릿 ID로 조회한다. 없으면 예외 발생. */
    private Template findTemplate(Long templateId) {
        log.info("findTemplate 시작 - templateId: {}", templateId);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NoSuchElementException("템플릿 없음: " + templateId));

        log.info("findTemplate 종료 - templateId: {}, title: {}", templateId, template.getTitle());
        return template;
    }

    /** 전체 템플릿 목록을 조회한다. */
    private List<Template> findAllTemplates() {
        log.info("findAllTemplates 시작");

        List<Template> templates = templateRepository.findAll();

        log.info("findAllTemplates 종료 - count: {}", templates.size());
        return templates;
    }

    /** 키워드/카테고리로 템플릿을 검색한다. */
    private List<Template> searchTemplates(String keyword, String category) {
        log.info("searchTemplates 시작 - keyword: {}, category: {}", keyword, category);

        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        String cat = (category != null && category.isBlank()) ? null : category;

        List<Template> templates  = templateRepository.search(kw, cat);

        log.info("searchTemplates 종료 - count: {}", templates.size());
        return templates;
    }

    /** 즐겨찾기 목록을 조회한다. */
    private List<Template> findFavorites() {
        log.info("findFavorites 시작");

        List<Template> favorites = templateRepository.findByIsFavoriteTrue();

        log.info("findFavorites 종료 - count: {}", favorites.size());
        return favorites;
    }

    /** 템플릿을 저장한다. */
    private Template saveTemplate(TemplateCreateRequest request) {
        log.info("saveTemplate 시작 - title: {}", request.getTitle());

        Template template = Template.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();
        Template saved = templateRepository.save(template);

        log.info("saveTemplate 종료 - title: {}, templateId: {}", request.getTitle(), saved.getId());
        return saved;
    }

    /** 태그를 저장한다. */
    private void saveTag(Template template, String tag) {
        log.info("saveTag 시작 - templateId: {}, tag: {}", template.getId(), tag);

        if (tag == null || tag.isBlank()) return;
        TemplateTag templateTag = TemplateTag.builder().template(template).tag(tag.trim()).build();
        templateTagRepository.save(templateTag);

        log.info("saveTag 종료 - templateId: {}, tag: {}", template.getId(), tag);
    }

    /** 템플릿 기본 정보를 수정한다. */
    private void updateTemplateInfo(Template template, TemplateCreateRequest request) {
        log.info("updateTemplateInfo 시작 - templateId: {}", template.getId());

        template.update(request.getTitle(), request.getDescription(), request.getCategory());

        log.info("updateTemplateInfo 종료 - templateId: {}, title: {}", template.getId(), template.getTitle());
    }

    /** 즐겨찾기 상태를 토글한다. */
    private void toggleFavorite(Template template) {
        log.info("toggleFavorite 시작 - templateId: {}, before: {}", template.getId(), template.isFavorite());

        template.toggleFavorite();

        log.info("toggleFavorite 종료 - templateId: {}, after: {}", template.getId(), template.isFavorite());
    }

    /** 블록을 생성하고 저장한다. */
    private TemplateBlock saveBlock(Template template, BlockRequest blockRequest, int sortOrder) {
        log.info("saveBlock 시작 - templateId: {}, blockType: {}, sortOrder: {}",
                template.getId(), blockRequest.getBlockType(), sortOrder);

        TemplateBlock block = TemplateBlock.builder()
                .template(template)
                .blockType(blockRequest.getBlockType())
                .content(blockRequest.getContent())
                .slotType(blockRequest.getSlotType())
                .sortOrder(sortOrder)
                .build();
        TemplateBlock saved = templateBlockRepository.save(block);

        log.info("saveBlock 종료 - templateId: {}, blockId: {}", template.getId(), saved.getId());
        return saved;
    }

    /** SELECT 슬롯의 선택지를 저장한다. */
    private void saveSlotOption(TemplateBlock block, SlotOptionRequest optionRequest, int sortOrder) {
        log.info("saveSlotOption 시작 - blockId: {}, label: {}", block.getId(), optionRequest.getLabel());

        SlotOption option = SlotOption.builder()
                .block(block)
                .label(optionRequest.getLabel())
                .sortOrder(sortOrder)
                .build();
        slotOptionRepository.save(option);

        log.info("saveSlotOption 종료 - blockId: {}, label: {}", block.getId(), optionRequest.getLabel());
    }

    /** 템플릿 존재 여부를 확인한다. 없으면 예외 발생. */
    private void checkExists(Long templateId) {
        log.info("checkExists 시작 - templateId: {}", templateId);

        if (!templateRepository.existsById(templateId)) {
            throw new NoSuchElementException("템플릿 없음: " + templateId);
        }

        log.info("checkExists 종료 - templateId: {}", templateId);
    }

    // ════════════════════════════════════════════════════════════
    // Phase 5 — Facade 메서드
    // ════════════════════════════════════════════════════════════

    /** 템플릿 복제 (블록/태그까지 동일하게 복사, 제목에 "(복사본)" 추가) */
    @Transactional
    public TemplateResponse cloneTemplateFacade(Long templateId) {
        // 1. 원본 조회
        Template original = this.findTemplate(templateId);

        // 2. 새 템플릿 생성 (제목에 "(복사본)" 추가)
        Template cloned = this.cloneTemplate(original);

        // 3. 태그 복사
        for (TemplateTag tag : original.getTags()) {
            this.saveTag(cloned, tag.getTag());
        }

        // 4. 블록/옵션 복사
        for (TemplateBlock block : original.getBlocks()) {
            TemplateBlock clonedBlock = this.cloneBlock(cloned, block);
            if (block.getBlockType() == BlockType.SLOT && block.getSlotType() == SlotType.SELECT) {
                int optionOrder = 0;
                for (SlotOption option : block.getOptions()) {
                    this.cloneSlotOption(clonedBlock, option, optionOrder++);
                }
            }
        }

        // 5. 재조회 후 반환
        Template result = this.findTemplate(cloned.getId());
        return new TemplateResponse(result);
    }

    /** 내보내기용 템플릿 데이터 조회 (TemplateCreateRequest 형태로 반환) */
    public TemplateCreateRequest exportTemplateFacade(Long templateId) {
        // 1. 템플릿 조회
        Template template = this.findTemplate(templateId);

        // 2. TemplateCreateRequest 형태로 변환
        return this.toCreateRequest(template);
    }

    /** 가져오기 (TemplateCreateRequest를 받아 새 템플릿으로 저장) */
    @Transactional
    public TemplateResponse importTemplateFacade(TemplateCreateRequest request) {
        // createTemplateFacade와 동일한 로직으로 처리
        return this.createTemplateFacade(request);
    }

    /** 입력값 히스토리 저장 */
    @Transactional
    public void saveHistoryFacade(Long templateId, HistorySaveRequest request) {
        // 1. 템플릿 존재 확인
        Template template = this.findTemplate(templateId);

        // 2. 슬롯값 JSON 직렬화 후 저장
        this.saveHistory(template, request.getSlotValues());
    }

    /** 입력값 히스토리 조회 (최근 5개) */
    public List<HistoryResponse> getHistoryFacade(Long templateId) {
        // 1. 히스토리 조회
        List<TemplateHistory> histories = this.findHistories(templateId);

        // 2. 응답 변환
        List<HistoryResponse> result = new ArrayList<>();
        for (TemplateHistory history : histories) {
            result.add(new HistoryResponse(history));
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════
    // Phase 5 — Private 메서드
    // ════════════════════════════════════════════════════════════

    /** 원본 템플릿을 복제한다. 제목에 "(복사본)"을 추가한다. */
    private Template cloneTemplate(Template original) {
        log.info("cloneTemplate 시작 - templateId: {}", original.getId());

        Template cloned = Template.builder()
                .title(original.getTitle() + " (복사본)")
                .description(original.getDescription())
                .category(original.getCategory())
                .build();
        Template saved = templateRepository.save(cloned);

        log.info("cloneTemplate 종료 - originalId: {}, clonedId: {}", original.getId(), saved.getId());
        return saved;
    }

    /** 블록을 복제한다. */
    private TemplateBlock cloneBlock(Template template, TemplateBlock original) {
        log.info("cloneBlock 시작 - templateId: {}, blockId: {}", template.getId(), original.getId());

        TemplateBlock cloned = TemplateBlock.builder()
                .template(template)
                .blockType(original.getBlockType())
                .content(original.getContent())
                .slotType(original.getSlotType())
                .sortOrder(original.getSortOrder())
                .build();
        TemplateBlock saved = templateBlockRepository.save(cloned);

        log.info("cloneBlock 종료 - originalBlockId: {}, clonedBlockId: {}", original.getId(), saved.getId());
        return saved;
    }

    /** 슬롯 옵션을 복제한다. */
    private void cloneSlotOption(TemplateBlock block, SlotOption original, int sortOrder) {
        log.info("cloneSlotOption 시작 - blockId: {}, label: {}", block.getId(), original.getLabel());

        SlotOption cloned = SlotOption.builder()
                .block(block)
                .label(original.getLabel())
                .sortOrder(sortOrder)
                .build();
        slotOptionRepository.save(cloned);

        log.info("cloneSlotOption 종료 - blockId: {}, label: {}", block.getId(), original.getLabel());
    }

    /** 템플릿을 TemplateCreateRequest 형태로 변환한다 (내보내기용). */
    private TemplateCreateRequest toCreateRequest(Template template) {
        log.info("toCreateRequest 시작 - templateId: {}", template.getId());

        TemplateCreateRequest request = new TemplateCreateRequest();
        setRequestField(request, "title", template.getTitle());
        setRequestField(request, "description", template.getDescription());
        setRequestField(request, "category", template.getCategory());
        setRequestField(request, "tags", template.getTags().stream().map(TemplateTag::getTag).toList());

        List<BlockRequest> blocks = new ArrayList<>();
        for (TemplateBlock block : template.getBlocks()) {
            BlockRequest br = new BlockRequest();
            setRequestField(br, "blockType", block.getBlockType());
            setRequestField(br, "content", block.getContent());
            setRequestField(br, "slotType", block.getSlotType());
            List<SlotOptionRequest> options = new ArrayList<>();
            for (SlotOption opt : block.getOptions()) {
                SlotOptionRequest sor = new SlotOptionRequest();
                setRequestField(sor, "label", opt.getLabel());
                options.add(sor);
            }
            setRequestField(br, "options", options);
            blocks.add(br);
        }
        setRequestField(request, "blocks", blocks);

        log.info("toCreateRequest 종료 - templateId: {}, blockCount: {}", template.getId(), blocks.size());
        return request;
    }

    /** 히스토리를 저장한다. */
    private void saveHistory(Template template, Map<String, String> slotValues) {
        log.info("saveHistory 시작 - templateId: {}", template.getId());

        String json;
        try {
            json = objectMapper.writeValueAsString(slotValues);
        } catch (JacksonIOException e) {
            json = "{}";
        }
        TemplateHistory history = TemplateHistory.builder()
                .template(template)
                .slotValues(json)
                .build();
        templateHistoryRepository.save(history);

        log.info("saveHistory 종료 - templateId: {}", template.getId());
    }

    /** 최근 히스토리 5개를 조회한다. */
    private List<TemplateHistory> findHistories(Long templateId) {
        log.info("findHistories 시작 - templateId: {}", templateId);

        List<TemplateHistory> histories = templateHistoryRepository
                .findByTemplateIdOrderByCreatedAtDesc(templateId, PageRequest.of(0, 5));

        log.info("findHistories 종료 - templateId: {}, count: {}", templateId, histories.size());
        return histories;
    }

    /** 리플렉션으로 Request DTO 필드를 설정한다 (내보내기 변환 전용). */
    private void setRequestField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            log.warn("setRequestField 실패 - field: {}", fieldName);
        }
    }
}
