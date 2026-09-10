package com.glowuprizz.lead_crm.repository.projection;

/** 채널별 방문/방문자 집계 결과(네이티브 쿼리 인터페이스 프로젝션). */
public interface ChannelVisitCount {
    String getChannel();

    long getVisits();

    long getVisitors();
}
