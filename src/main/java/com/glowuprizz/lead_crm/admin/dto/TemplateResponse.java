package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.domain.HtmlTemplate;

import java.time.LocalDateTime;

public record TemplateResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {
    public static TemplateResponse from(HtmlTemplate t) {
        return new TemplateResponse(t.getId(), t.getName(), t.getCreatedAt());
    }
}
