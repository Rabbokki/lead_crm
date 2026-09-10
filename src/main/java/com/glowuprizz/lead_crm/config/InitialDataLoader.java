package com.glowuprizz.lead_crm.config;

import com.glowuprizz.lead_crm.domain.User;
import com.glowuprizz.lead_crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitialDataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    @Bean
    public ApplicationRunner seedAdmin() {
        return args -> {
            String email = props.admin().email();
            if (userRepository.findByEmail(email).isEmpty()) {
                userRepository.save(User.of(email, passwordEncoder.encode(props.admin().password())));
                System.out.println("[init] admin account created: " + email);
            }
        };
    }
}