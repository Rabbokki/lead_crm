package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.CampaignDetailView;
import com.glowuprizz.lead_crm.admin.dto.CampaignResponse;
import com.glowuprizz.lead_crm.admin.dto.CampaignStatsResponse;
import com.glowuprizz.lead_crm.admin.dto.FormResponse;
import com.glowuprizz.lead_crm.admin.dto.LeadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 관리자 Thymeleaf 화면에 필요한 뷰 모델을 조립한다.
 * 조회 로직은 기존 서비스에 위임하고, 컨트롤러는 이 서비스가 만든 모델을 그대로 렌더링만 한다.
 */
@Service
@RequiredArgsConstructor
public class AdminPageService {

    private final CampaignService campaignService;
    private final StatsService statsService;
    private final FormService formService;
    private final LinkService linkService;
    private final LeadService leadService;
    private final TemplateService templateService;

    /** 캠페인 목록 화면: 캠페인별 요약 통계 행. */
    @Transactional(readOnly = true)
    public List<CampaignStatsResponse> campaignRows(Long userId) {
        List<CampaignResponse> campaigns = campaignService.list(userId);
        List<CampaignStatsResponse> rows = new ArrayList<>();
        for (CampaignResponse c : campaigns) {
            rows.add(statsService.campaignStats(userId, c.id()));
        }
        return rows;
    }

    /** 캠페인 상세 화면: 요약·채널별·폼/링크·신청 명단. */
    @Transactional(readOnly = true)
    public CampaignDetailView campaignDetail(Long userId, Long campaignId) {
        CampaignStatsResponse summary = statsService.campaignStats(userId, campaignId);

        List<FormResponse> forms = formService.listByCampaign(userId, campaignId);
        List<CampaignDetailView.FormView> formViews = new ArrayList<>();
        List<LeadResponse> leads = new ArrayList<>();
        for (FormResponse form : forms) {
            formViews.add(new CampaignDetailView.FormView(
                    form.id(),
                    form.name(),
                    form.fieldSchema(),
                    linkService.list(userId, form.id())));
            leads.addAll(leadService.listByForm(userId, form.id()));
        }
        leads.sort(Comparator.comparing(LeadResponse::createdAt).reversed());

        return new CampaignDetailView(
                summary,
                statsService.channelStats(userId, campaignId),
                templateService.list(userId),
                formViews,
                leads);
    }
}
