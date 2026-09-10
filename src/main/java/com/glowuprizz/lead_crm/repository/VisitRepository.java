package com.glowuprizz.lead_crm.repository;

import com.glowuprizz.lead_crm.domain.Visit;
import com.glowuprizz.lead_crm.repository.projection.ChannelVisitCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    /** 캠페인 소속 모든 폼의 방문(행) 수. */
    @Query(value = """
            SELECT COUNT(*)
            FROM visits v JOIN forms f ON v.form_id = f.id
            WHERE f.campaign_id = :campaignId
            """, nativeQuery = true)
    long countVisitsByCampaign(@Param("campaignId") Long campaignId);

    /** 캠페인 소속 모든 폼의 고유 방문자(visitor_id DISTINCT) 수. */
    @Query(value = """
            SELECT COUNT(DISTINCT v.visitor_id)
            FROM visits v JOIN forms f ON v.form_id = f.id
            WHERE f.campaign_id = :campaignId
            """, nativeQuery = true)
    long countVisitorsByCampaign(@Param("campaignId") Long campaignId);

    /** 채널별 방문/방문자 집계. 데이터가 있는 채널만 반환하므로 없는 채널은 서비스에서 0으로 채운다. */
    @Query(value = """
            SELECT v.channel AS channel,
                   COUNT(*) AS visits,
                   COUNT(DISTINCT v.visitor_id) AS visitors
            FROM visits v JOIN forms f ON v.form_id = f.id
            WHERE f.campaign_id = :campaignId AND v.channel IS NOT NULL
            GROUP BY v.channel
            """, nativeQuery = true)
    List<ChannelVisitCount> countVisitsByChannel(@Param("campaignId") Long campaignId);
}
