package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.common.Channel;
import com.glowuprizz.lead_crm.domain.Link;

public record LinkResponse(
        Long id,
        Channel channel,
        String code,
        String url
) {
    public static LinkResponse from(Link link, String url) {
        return new LinkResponse(link.getId(), link.getChannel(), link.getCode(), url);
    }
}
