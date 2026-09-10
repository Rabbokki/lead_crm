package com.glowuprizz.lead_crm.repository;

import com.glowuprizz.lead_crm.domain.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, Long> {
    List<Form> findAllByCampaignId(Long campaignId);
}