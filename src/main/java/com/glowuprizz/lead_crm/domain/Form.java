package com.glowuprizz.lead_crm.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "forms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_schema", nullable = false, columnDefinition = "jsonb")
    private List<FieldDef> fieldSchema;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static Form of(Long campaignId, Long templateId, String name, List<FieldDef> schema) {
        Form f = new Form();
        f.campaignId = campaignId;
        f.templateId = templateId;
        f.name = name;
        f.fieldSchema = schema;
        return f;
    }
}