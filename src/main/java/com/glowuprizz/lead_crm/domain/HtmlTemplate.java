package com.glowuprizz.lead_crm.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "html_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HtmlTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "raw_html", nullable = false, columnDefinition = "text")
    private String rawHtml;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static HtmlTemplate of(Long userId, String name, String rawHtml) {
        HtmlTemplate t = new HtmlTemplate();
        t.userId = userId;
        t.name = name;
        t.rawHtml = rawHtml;
        return t;
    }
}