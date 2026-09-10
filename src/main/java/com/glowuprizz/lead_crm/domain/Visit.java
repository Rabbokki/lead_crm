package com.glowuprizz.lead_crm.domain;

import com.glowuprizz.lead_crm.common.Channel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "link_id")
    private Long linkId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Channel channel;

    @Column(name = "visitor_id", nullable = false, length = 64)
    private String visitorId;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(columnDefinition = "text")
    private String referrer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static Visit of(Long formId, Long linkId, Channel channel,
                           String visitorId, String ip, String userAgent, String referrer) {
        Visit v = new Visit();
        v.formId = formId;
        v.linkId = linkId;
        v.channel = channel;
        v.visitorId = visitorId;
        v.ip = ip;
        v.userAgent = userAgent;
        v.referrer = referrer;
        return v;
    }
}