package com.glowuprizz.lead_crm.admin.dto;

import com.glowuprizz.lead_crm.domain.FieldDef;

import java.util.List;

/** 캠페인 상세 화면(campaign-detail.html)에 필요한 데이터를 한데 모은 뷰 모델. */
public record CampaignDetailView(
        CampaignStatsResponse summary,
        List<ChannelStatsResponse> channels,
        List<TemplateResponse> templates,
        List<FormView> forms,
        List<LeadResponse> leads
) {
    public record FormView(
            Long id,
            String name,
            List<FieldDef> fieldSchema,
            List<LinkResponse> links
    ) {
    }
}
