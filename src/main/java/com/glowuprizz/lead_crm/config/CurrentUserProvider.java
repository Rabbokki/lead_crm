package com.glowuprizz.lead_crm.config;

import com.glowuprizz.lead_crm.common.NotFoundException;
import com.glowuprizz.lead_crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 현재 로그인한 운영자의 식별자를 SecurityContext(인증 name = email)에서 조회한다.
 * 컨트롤러는 이 헬퍼로 userId를 얻어 서비스에 소유권 검증용으로 전달한다.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new NotFoundException("authenticated user not found");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("authenticated user not found"))
                .getId();
    }
}
