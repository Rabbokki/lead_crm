package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.CampaignCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.CampaignResponse;
import com.glowuprizz.lead_crm.common.NotFoundException;
import com.glowuprizz.lead_crm.domain.Campaign;
import com.glowuprizz.lead_crm.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;

    @Transactional
    public CampaignResponse create(Long userId, CampaignCreateRequest req) {
        Campaign saved = campaignRepository.save(
                Campaign.of(userId, req.name(), req.description()));
        return CampaignResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> list(Long userId) {
        return campaignRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(CampaignResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampaignResponse get(Long userId, Long campaignId) {
        return CampaignResponse.from(getOwned(userId, campaignId));
    }

    /** 소유한 캠페인 엔티티를 반환한다. 없거나 타인 소유면 404. */
    @Transactional(readOnly = true)
    public Campaign getOwned(Long userId, Long campaignId) {
        Campaign c = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("campaign not found: " + campaignId));
        if (!c.getUserId().equals(userId)) {
            throw new NotFoundException("campaign not found: " + campaignId);
        }
        return c;
    }
}
