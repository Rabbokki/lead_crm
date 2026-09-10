package com.glowuprizz.lead_crm.domain;

import com.glowuprizz.lead_crm.common.Channel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static Link of(Long formId, Channel channel, String code) {
        Link l = new Link();
        l.formId = formId;
        l.channel = channel;
        l.code = code;
        return l;
    }
}