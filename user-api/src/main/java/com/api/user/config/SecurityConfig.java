package com.api.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt com custo 12 — equilíbrio entre segurança e desempenho.
     * Nunca use MD5, SHA-1 ou SHA-256 direto para senhas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configura a cadeia de filtros HTTP:
     * - Desabilita CSRF (API stateless com JWT — adapte se usar sessão)
     * - Sessão stateless (sem HttpSession)
     * - Headers de segurança (HSTS, X-Frame-Options, CSP, etc.)
     * - Desabilita o formulário de login padrão do Spring Security
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API stateless — sem sessão
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Desabilita CSRF (adequado para APIs REST puras)
            .csrf(AbstractHttpConfigurer::disable)

            // Headers de segurança
            .headers(headers -> headers
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'none'"))
                .referrerPolicy(ref ->
                    ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .frameOptions(fo -> fo.deny())
            )

            // Autorização — adapte conforme sua estratégia de autenticação
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/users").permitAll()
                .anyRequest().denyAll()
            )

            // Desabilita login form e HTTP Basic
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
