package br.com.equilibra.auth.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenServiceTest {

    // Use a fixed instant truncated to seconds for deterministic testing
    // JWT timestamps have second precision only
    private static final Instant NOW = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).minusSeconds(60);

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void shouldIssueValidTokenWithExpectedClaims() {
        // Use a fixed clock for deterministic testing
        JwtTokenService testTokenService = new JwtTokenService(
            jwtEncoder,
            new br.com.equilibra.auth.infrastructure.jwt.JwtProperties("equilibra-api", Duration.ofMinutes(15), "test-secret-with-at-least-32-bytes-for-hs256-algorithm"),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );

        LoginTokenResponse response = testTokenService.issueAccessToken(
            new AuthenticatedUser("123e4567-e89b-12d3-a456-426614174000", "user@example.com")
        );

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("equilibra-api");
        assertThat(jwt.getSubject()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getClaims()).doesNotContainKeys("email", "password", "passwordHash");
    }

    @Test
    void shouldGenerateUniqueJtiForEachToken() {
        // Use a fixed clock for deterministic testing
        JwtTokenService testTokenService = new JwtTokenService(
            jwtEncoder,
            new br.com.equilibra.auth.infrastructure.jwt.JwtProperties("equilibra-api", Duration.ofMinutes(15), "test-secret-with-at-least-32-bytes-for-hs256-algorithm"),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );

        Jwt first = jwtDecoder.decode(testTokenService.issueAccessToken(
            new AuthenticatedUser("123e4567-e89b-12d3-a456-426614174000", "user@example.com")
        ).accessToken());
        Jwt second = jwtDecoder.decode(testTokenService.issueAccessToken(
            new AuthenticatedUser("123e4567-e89b-12d3-a456-426614174000", "user@example.com")
        ).accessToken());

        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void shouldRejectTamperedToken() {
        // Use a fixed clock for deterministic testing
        JwtTokenService testTokenService = new JwtTokenService(
            jwtEncoder,
            new br.com.equilibra.auth.infrastructure.jwt.JwtProperties("equilibra-api", Duration.ofMinutes(15), "test-secret-with-at-least-32-bytes-for-hs256-algorithm"),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );

        String token = testTokenService.issueAccessToken(
            new AuthenticatedUser("123e4567-e89b-12d3-a456-426614174000", "user@example.com")
        ).accessToken();
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtDecoder.decode(tampered))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectExpiredToken() {
        // Create a new service with short TTL and past clock
        JwtTokenService expiredTokenService = new JwtTokenService(
            jwtEncoder,
            new br.com.equilibra.auth.infrastructure.jwt.JwtProperties("equilibra-api", Duration.ofSeconds(1), "test-secret-with-at-least-32-bytes-for-hs256-algorithm"),
            Clock.fixed(NOW.minus(Duration.ofMinutes(5)), ZoneOffset.UTC)
        );

        String token = expiredTokenService.issueAccessToken(
            new AuthenticatedUser("123e4567-e89b-12d3-a456-426614174000", "user@example.com")
        ).accessToken();

        assertThatThrownBy(() -> jwtDecoder.decode(token))
            .isInstanceOf(JwtException.class);
    }
}