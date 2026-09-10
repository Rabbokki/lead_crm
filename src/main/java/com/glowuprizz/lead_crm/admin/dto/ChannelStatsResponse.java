package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.common.Channel;

public record ChannelStatsResponse(
        Channel channel,
        long visits,
        long visitors,
        long submissions,
        double conversionRate
) {
}
