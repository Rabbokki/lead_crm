package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.CampaignStatsResponse;
import com.glowuprizz.lead_crm.admin.dto.ChannelStatsResponse;
import com.glowuprizz.lead_crm.common.Channel;
import com.glowuprizz.lead_crm.domain.Campaign;
import com.glowuprizz.lead_crm.repository.LeadRepository;
import com.glowuprizz.lead_crm.repository.VisitRepository;
import com.glowuprizz.lead_crm.repository.projection.ChannelSubmissionCount;
import com.glowuprizz.lead_crm.repository.projection.ChannelVisitCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 캠페인·채널 전환 통계 집계 전용 서비스.
 * 방문/방문자/신청 집계는 네이티브 SQL로 수행하고, 전환율만 애플리케이션에서 계산한다.
 * 소유권 검증은 CampaignService.getOwned에 위임한다(타인 캠페인이면 404).
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final CampaignService campaignService;
    private final VisitRepository visitRepository;
    private final LeadRepository leadRepository;

    @Transactional(readOnly = true)
    public CampaignStatsResponse campaignStats(Long userId, Long campaignId) {
        Campaign campaign = campaignService.getOwned(userId, campaignId);

        long visits = visitRepository.countVisitsByCampaign(campaignId);
        long visitors = visitRepository.countVisitorsByCampaign(campaignId);
        long submissions = leadRepository.countSubmissionsByCampaign(campaignId);

        return new CampaignStatsResponse(
                campaign.getId(),
                campaign.getName(),
                visits,
                visitors,
                submissions,
                conversionRate(submissions, visitors));
    }

    @Transactional(readOnly = true)
    public List<ChannelStatsResponse> channelStats(Long userId, Long campaignId) {
        campaignService.getOwned(userId, campaignId);

        Map<String, ChannelVisitCount> visitByChannel = new HashMap<>();
        for (ChannelVisitCount row : visitRepository.countVisitsByChannel(campaignId)) {
            visitByChannel.put(row.getChannel(), row);
        }
        Map<String, Long> submissionByChannel = new HashMap<>();
        for (ChannelSubmissionCount row : leadRepository.countSubmissionsByChannel(campaignId)) {
            submissionByChannel.put(row.getChannel(), row.getSubmissions());
        }

        // Channel enum 4종을 항상 모두 반환한다. 데이터가 없는 채널은 0으로 채운다.
        List<ChannelStatsResponse> result = new ArrayList<>();
        for (Channel channel : Channel.values()) {
            ChannelVisitCount v = visitByChannel.get(channel.name());
            long visits = v == null ? 0 : v.getVisits();
            long visitors = v == null ? 0 : v.getVisitors();
            long submissions = submissionByChannel.getOrDefault(channel.name(), 0L);
            result.add(new ChannelStatsResponse(
                    channel, visits, visitors, submissions,
                    conversionRate(submissions, visitors)));
        }
        return result;
    }

    /** 전환율 = 신청 ÷ 방문자 × 100. 방문자 0이면 0.0. 소수점 1자리 반올림. */
    static double conversionRate(long submissions, long visitors) {
        if (visitors == 0) {
            return 0.0;
        }
        double raw = (double) submissions / visitors * 100.0;
        return Math.round(raw * 10.0) / 10.0;
    }
}
