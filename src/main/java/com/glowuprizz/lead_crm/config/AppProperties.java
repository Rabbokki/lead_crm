package com.glowuprizz.lead_crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Admin admin, String publicBaseUrl, String adminBaseUrl) {

    public record Admin(String email, String password) {
    }
}