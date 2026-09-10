package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.domain.FieldDef;
import com.glowuprizz.lead_crm.domain.Form;

import java.time.LocalDateTime;
import java.util.List;

public record FormResponse(
        Long id,
        Long campaignId,
        Long templateId,
        String name,
        List<FieldDef> fieldSchema,
        String status,
        LocalDateTime createdAt
) {
    public static FormResponse from(Form f) {
        return new FormResponse(
                f.getId(),
                f.getCampaignId(),
                f.getTemplateId(),
                f.getName(),
                f.getFieldSchema(),
                f.getStatus(),
                f.getCreatedAt()
        );
    }
}
