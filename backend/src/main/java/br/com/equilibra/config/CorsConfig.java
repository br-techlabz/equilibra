package br.com.equilibra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de CORS para permitir requisições do frontend Angular.
 * <p>
 * Configuração restritiva apropriada para desenvolvimento.
 * Origins configuráveis via variável de ambiente CORS_ALLOWED_ORIGINS.
 * </p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origins permitidos (configurável via env var, default para Angular dev server)
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        } else {
            configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        }

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Request-ID",
            "Accept",
            "Origin"
        ));

        // Headers expostos ao frontend
        configuration.setExposedHeaders(Arrays.asList("X-Request-ID"));

        // Permitir credenciais (cookies, Authorization header)
        configuration.setAllowCredentials(true);

        // Cache de preflight por 1 hora
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}