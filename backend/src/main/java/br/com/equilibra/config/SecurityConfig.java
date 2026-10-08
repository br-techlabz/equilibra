package br.com.equilibra.config;

import br.com.equilibra.auth.infrastructure.security.JwtAuthenticationConverter;
import br.com.equilibra.shared.web.security.SecurityProblemSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração temporária de segurança.
 * <p>
 * ATENÇÃO: Esta configuração é TEMPORÁRIA e será substituída na Sprint de Autenticação.
 * Atualmente permite acesso livre aos endpoints de health, actuator e documentação OpenAPI
 * para facilitar o desenvolvimento.
 * </p>
 *
 * <strong>Não utilizar em produção.</strong>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtAuthenticationConverter jwtAuthenticationConverter,
        SecurityProblemSupport securityProblemSupport
    ) throws Exception {
        http
            // Desabilita CSRF para APIs REST stateless
            .csrf(csrf -> csrf.disable())

            // Configuração de sessão stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Autorização de endpoints
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos de autenticação
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh", "/auth/logout").permitAll()

                // Endpoints públicos (health, actuator, docs)
                .requestMatchers(
                    "/actuator/health/**",
                    "/actuator/info",
                    "/actuator/metrics/**",
                    "/actuator/prometheus",
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Demais endpoints exigem autenticação por padrão.
                .anyRequest().authenticated()
            )

            // Tratamento de erros de segurança em Problem Details
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(securityProblemSupport)
                .accessDeniedHandler(securityProblemSupport)
            )

            // Resource Server JWT stateless
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(securityProblemSupport)
                .accessDeniedHandler(securityProblemSupport)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            )

            // Headers de segurança básicos
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(contentType -> contentType.disable())
            );

        return http.build();
    }
}