package com.glowuprizz.lead_crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 관리자 서버(8080) 전용 보안 설정. 세션 기반 폼 로그인 + /admin/**·/api/admin/** 인증 필수.
 * publicform 프로파일에는 로드되지 않으므로 공개 폼 서버에는 관리자 인증 기능이 존재하지 않는다.
 */
@Configuration
@Profile("admin")
public class AdminSecurityConfig {

    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        // 미인증 상태로 /api/** 에 접근하면 로그인 페이지로 리다이렉트(302)하지 않고 401을 반환한다.
        // (CLAUDE.md HTTP 상태 규칙: 미인증 관리자 API 접근 → 401)
        RequestMatcher apiMatcher = request -> request.getRequestURI().startsWith("/api/");
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/f/**", "/api/public/**").permitAll()
                        .requestMatchers("/login", "/error", "/css/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiMatcher))
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/admin/campaigns", true)
                        .permitAll())
                .logout(out -> out.logoutSuccessUrl("/login"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }
}
