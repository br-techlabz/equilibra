package br.com.equilibra.auth.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configurações centralizadas para emissão e validação de JWT.
 */
@ConfigurationProperties(prefix = "equilibra.security.jwt")
public record JwtProperties(
    String issuer,
    Duration accessTokenTtl,
    String secret
) {

    private static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("JWT issuer must be configured");
        }
        if (accessTokenTtl == null || accessTokenTtl.isNegative() || accessTokenTtl.isZero()) {
            throw new IllegalStateException("JWT access token TTL must be positive");
        }
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must have at least 32 bytes");
        }
    }
}
