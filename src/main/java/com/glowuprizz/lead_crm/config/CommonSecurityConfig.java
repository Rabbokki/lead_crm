package com.glowuprizz.lead_crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 프로파일과 무관하게 필요한 보안 빈.
 * PasswordEncoder는 관리자 로그인(admin)과 계정 시딩(InitialDataLoader, 양쪽 프로파일)에서 모두 쓰이므로
 * 프로파일별 SecurityConfig가 아니라 이 공용 설정에 둔다.
 */
@Configuration
public class CommonSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
