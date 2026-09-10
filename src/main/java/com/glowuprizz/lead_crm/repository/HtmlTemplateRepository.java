package com.glowuprizz.lead_crm.repository;

import com.glowuprizz.lead_crm.domain.HtmlTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HtmlTemplateRepository extends JpaRepository<HtmlTemplate, Long> {
    List<HtmlTemplate> findAllByUserIdOrderByIdDesc(Long userId);
}