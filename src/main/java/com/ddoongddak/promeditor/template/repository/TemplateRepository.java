package com.ddoongddak.promeditor.template.repository;

import com.ddoongddak.promeditor.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findByIsFavoriteTrue();

    @Query("SELECT DISTINCT t FROM Template t LEFT JOIN t.tags tag WHERE " +
           "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(tag.tag) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR t.category = :category)")
    List<Template> search(@Param("keyword") String keyword,
                          @Param("category") String category);
}
