package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.common.Channel;
import com.glowuprizz.lead_crm.domain.Lead;

import java.time.LocalDateTime;
import java.util.Map;

public record LeadResponse(
        Long id,
        Channel channel,
        String visitorId,
        Map<String, Object> payload,
        LocalDateTime createdAt
) {
    public static LeadResponse from(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getChannel(),
                lead.getVisitorId(),
                lead.getPayload(),
                lead.getCreatedAt()
        );
    }
}
