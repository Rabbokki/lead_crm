package com.glowuprizz.lead_crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 공개 폼 서버(8081) 전용 보안 설정.
 * 관리자 인증 기능(폼 로그인·UserDetailsService)이 없어야 하므로 formLogin을 두지 않는다.
 * 공개 경로만 허용하고 나머지는 전부 거부한다. 세션은 만들지 않으며(STATELESS),
 * 업로드 HTML의 &lt;form&gt;이 그대로 POST 제출할 수 있도록 CSRF는 비활성화한다.
 *
 * <p>신청 폼은 sandbox iframe(allow-same-origin 없음) 안에서 렌더링되고, 그 안에서 제출된
 * 결과 페이지(submit-success)도 같은 iframe에 표시된다. 스프링 시큐리티 기본값인
 * {@code X-Frame-Options: DENY}는 이 동일 출처 프레이밍마저 막으므로 비활성화하고,
 * 클릭재킹 방어는 {@code CSP frame-ancestors 'self'}로 대체한다.
 * 에러 응답이 다시 거부되어 2차 403이 나지 않도록 {@code /error}도 permitAll에 포함한다.
 */
@Configuration
@Profile("publicform")
public class PublicSecurityConfig {

    @Bean
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/f/**", "/api/public/**", "/error").permitAll()
                        .anyRequest().denyAll())
                .headers(headers -> headers
                        // X-Frame-Options: DENY 제거(동일 출처 iframe 렌더링 허용).
                        .frameOptions(frame -> frame.disable())
                        // 클릭재킹 방어를 CSP frame-ancestors로 대체(외부 사이트의 프레이밍 차단).
                        .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self'")))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(out -> out.disable());
        return http.build();
    }
}
