package com.ddoongddak.promeditor.template.controller.api;

import com.ddoongddak.promeditor.template.dto.TemplateSummaryResponse;
import com.ddoongddak.promeditor.template.service.TemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TemplateApiController.class)
@WithMockUser
class TemplateApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemplateService templateService;

    @Test
    void getTemplates_정상요청_200반환() throws Exception {
        // given
        TemplateSummaryResponse summary = mock(TemplateSummaryResponse.class);
        given(summary.getId()).willReturn(1L);
        given(summary.getTitle()).willReturn("테스트 템플릿");
        given(templateService.getTemplateListFacade()).willReturn(List.of(summary));

        // when & then
        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("테스트 템플릿"));
    }

    @Test
    void createTemplate_유효한요청_201반환() throws Exception {
        // given
        String body = """
                {
                    "title": "새 템플릿",
                    "description": "설명",
                    "blocks": []
                }
                """;

        // when & then
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(templateService).createTemplateFacade(any());
    }

    @Test
    void createTemplate_제목없음_400반환() throws Exception {
        // given
        String body = """
                {
                    "title": "",
                    "blocks": []
                }
                """;

        // when & then
        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTemplate_존재하는ID_204반환() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/templates/1"))
                .andExpect(status().isNoContent());

        verify(templateService).deleteTemplateFacade(1L);
    }

    @Test
    void deleteTemplate_존재하지않는ID_404반환() throws Exception {
        // given
        willThrow(new NoSuchElementException("템플릿 없음"))
                .given(templateService).deleteTemplateFacade(eq(999L));

        // when & then
        mockMvc.perform(delete("/api/templates/999"))
                .andExpect(status().isNotFound());
    }
}
