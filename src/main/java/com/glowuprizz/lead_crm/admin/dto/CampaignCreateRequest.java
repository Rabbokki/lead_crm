package com.glowuprizz.lead_crm.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CampaignCreateRequest(
        @NotBlank String name,
        String description
) {
}
