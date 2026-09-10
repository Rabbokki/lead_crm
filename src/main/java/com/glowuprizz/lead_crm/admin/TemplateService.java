package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.TemplateCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.TemplateResponse;
import com.glowuprizz.lead_crm.common.NotFoundException;
import com.glowuprizz.lead_crm.domain.HtmlTemplate;
import com.glowuprizz.lead_crm.repository.HtmlTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final HtmlTemplateRepository templateRepository;

    @Transactional
    public TemplateResponse create(Long userId, TemplateCreateRequest req) {
        HtmlTemplate saved = templateRepository.save(
                HtmlTemplate.of(userId, req.name(), req.rawHtml()));
        return TemplateResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> list(Long userId) {
        return templateRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(TemplateResponse::from)
                .toList();
    }

    /** 소유한 템플릿 엔티티를 반환한다. 없거나 타인 소유면 404. */
    @Transactional(readOnly = true)
    public HtmlTemplate getOwned(Long userId, Long templateId) {
        HtmlTemplate t = templateRepository.findById(templateId)
                .orElseThrow(() -> new NotFoundException("template not found: " + templateId));
        if (!t.getUserId().equals(userId)) {
            throw new NotFoundException("template not found: " + templateId);
        }
        return t;
    }
}
