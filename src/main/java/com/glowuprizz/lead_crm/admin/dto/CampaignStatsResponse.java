package com.glowuprizz.lead_crm.admin.dto;

public record CampaignStatsResponse(
        Long campaignId,
        String campaignName,
        long visits,
        long visitors,
        long submissions,
        double conversionRate
) {
}
