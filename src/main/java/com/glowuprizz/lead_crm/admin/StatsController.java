package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.CampaignStatsResponse;
import com.glowuprizz.lead_crm.admin.dto.ChannelStatsResponse;
import com.glowuprizz.lead_crm.config.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
@Profile("admin")
public class StatsController {

    private final StatsService statsService;
    private final CurrentUserProvider currentUser;

    @GetMapping("/{id}/stats")
    public CampaignStatsResponse stats(@PathVariable Long id) {
        return statsService.campaignStats(currentUser.currentUserId(), id);
    }

    @GetMapping("/{id}/stats/channels")
    public List<ChannelStatsResponse> channelStats(@PathVariable Long id) {
        return statsService.channelStats(currentUser.currentUserId(), id);
    }
}
