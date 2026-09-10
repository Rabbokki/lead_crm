package com.glowuprizz.lead_crm.domain;

import com.glowuprizz.lead_crm.common.Channel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "leads")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lead {

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

    @Column(name = "visitor_id", length = 64)
    private String visitorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static Lead of(Long formId, Long linkId, Channel channel,
                          String visitorId, Map<String, Object> payload) {
        Lead l = new Lead();
        l.formId = formId;
        l.linkId = linkId;
        l.channel = channel;
        l.visitorId = visitorId;
        l.payload = payload;
        return l;
    }
}