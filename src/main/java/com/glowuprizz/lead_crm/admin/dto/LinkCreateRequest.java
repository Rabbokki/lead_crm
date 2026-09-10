package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.common.Channel;
import jakarta.validation.constraints.NotNull;

public record LinkCreateRequest(
        @NotNull Channel channel
) {
}
