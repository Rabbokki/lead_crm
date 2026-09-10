package com.glowuprizz.lead_crm.repository;

import com.glowuprizz.lead_crm.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findAllByUserIdOrderByIdDesc(Long userId);
}