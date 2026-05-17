package com.ddoongddak.promeditor.template.repository;

import com.ddoongddak.promeditor.template.entity.TemplateHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateHistoryRepository extends JpaRepository<TemplateHistory, Long> {

    List<TemplateHistory> findByTemplateIdOrderByCreatedAtDesc(Long templateId, Pageable pageable);
}
