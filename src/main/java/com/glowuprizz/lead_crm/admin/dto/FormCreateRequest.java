package com.glowuprizz.lead_crm.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FormCreateRequest(
        @NotNull Long templateId,
        @NotBlank String name
) {
}
