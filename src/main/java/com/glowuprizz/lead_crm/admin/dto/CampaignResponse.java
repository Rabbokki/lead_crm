package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.domain.Campaign;

import java.time.LocalDateTime;

public record CampaignResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static CampaignResponse from(Campaign c) {
        return new CampaignResponse(c.getId(), c.getName(), c.getDescription(), c.getCreatedAt());
    }
}
