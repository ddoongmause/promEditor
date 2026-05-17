package com.ddoongddak.promeditor.template.controller.api;

import com.ddoongddak.promeditor.common.ApiResponse;
import com.ddoongddak.promeditor.template.dto.*;
import com.ddoongddak.promeditor.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateApiController {

    private final TemplateService templateService;

    /** 목록 조회 (검색/카테고리 필터 지원) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateSummaryResponse>>> getTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<TemplateSummaryResponse> data;
        if (keyword != null || category != null) {
            data = templateService.searchTemplatesFacade(keyword, category);
        } else {
            data = templateService.getTemplateListFacade();
        }
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** 즐겨찾기 목록 조회 */
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<TemplateSummaryResponse>>> getFavorites() {
        List<TemplateSummaryResponse> data = templateService.getFavoritesFacade();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplate(@PathVariable Long id) {
        TemplateResponse data = templateService.getTemplateFacade(id);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** 생성 */
    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request) {
        TemplateResponse data = templateService.createTemplateFacade(request);
        URI location = URI.create("/api/templates/" + data.getId());
        return ResponseEntity.created(location).body(ApiResponse.ok(data));
    }

    /** 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateCreateRequest request) {
        TemplateResponse data = templateService.updateTemplateFacade(id, request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplateFacade(id);
        return ResponseEntity.noContent().build();
    }

    /** 즐겨찾기 토글 */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<TemplateResponse>> toggleFavorite(@PathVariable Long id) {
        TemplateResponse data = templateService.toggleFavoriteFacade(id);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** 복제 */
    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<TemplateResponse>> cloneTemplate(@PathVariable Long id) {
        TemplateResponse data = templateService.cloneTemplateFacade(id);
        URI location = URI.create("/api/templates/" + data.getId());
        return ResponseEntity.created(location).body(ApiResponse.ok(data));
    }

    /** 내보내기 (JSON) */
    @GetMapping("/{id}/export")
    public ResponseEntity<ApiResponse<TemplateCreateRequest>> exportTemplate(@PathVariable Long id) {
        TemplateCreateRequest data = templateService.exportTemplateFacade(id);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /** 가져오기 */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<TemplateResponse>> importTemplate(
            @Valid @RequestBody TemplateCreateRequest request) {
        TemplateResponse data = templateService.importTemplateFacade(request);
        URI location = URI.create("/api/templates/" + data.getId());
        return ResponseEntity.created(location).body(ApiResponse.ok(data));
    }

    /** 히스토리 저장 */
    @PostMapping("/{id}/histories")
    public ResponseEntity<Void> saveHistory(
            @PathVariable Long id,
            @Valid @RequestBody HistorySaveRequest request) {
        templateService.saveHistoryFacade(id, request);
        return ResponseEntity.noContent().build();
    }

    /** 히스토리 조회 (최근 5개) */
    @GetMapping("/{id}/histories")
    public ResponseEntity<ApiResponse<List<HistoryResponse>>> getHistories(@PathVariable Long id) {
        List<HistoryResponse> data = templateService.getHistoryFacade(id);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
