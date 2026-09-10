package com.glowuprizz.lead_crm.repository;

import com.glowuprizz.lead_crm.domain.Lead;
import com.glowuprizz.lead_crm.repository.projection.ChannelSubmissionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findAllByFormIdOrderByIdDesc(Long formId);

    /** 캠페인 소속 모든 폼의 신청(제출) 수. */
    @Query(value = """
            SELECT COUNT(*)
            FROM leads l JOIN forms f ON l.form_id = f.id
            WHERE f.campaign_id = :campaignId
            """, nativeQuery = true)
    long countSubmissionsByCampaign(@Param("campaignId") Long campaignId);

    /** 채널별 신청 집계. 데이터가 있는 채널만 반환하므로 없는 채널은 서비스에서 0으로 채운다. */
    @Query(value = """
            SELECT l.channel AS channel,
                   COUNT(*) AS submissions
            FROM leads l JOIN forms f ON l.form_id = f.id
            WHERE f.campaign_id = :campaignId AND l.channel IS NOT NULL
            GROUP BY l.channel
            """, nativeQuery = true)
    List<ChannelSubmissionCount> countSubmissionsByChannel(@Param("campaignId") Long campaignId);
}
