package com.glowuprizz.lead_crm.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record TemplateCreateRequest(
        @NotBlank String name,
        @NotBlank String rawHtml
) {
}
