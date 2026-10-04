package br.com.equilibra.auth.infrastructure.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

/**
 * Configuração de emissão e validação JWT via Spring Security/Nimbus.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        byte[] secret = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        // Ensure secret is exactly 32 bytes (256 bits) for HS256
        byte[] keyBytes = java.util.Arrays.copyOf(secret, 32);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
        return NimbusJwtEncoder.withSecretKey(secretKey)
            .algorithm(MacAlgorithm.HS256)
            .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        byte[] secret = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        // Use exactly 32 bytes for consistency
        byte[] keyBytes = java.util.Arrays.copyOf(secret, 32);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);

        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

        // Validar issuer usando o validator padrão do Spring Security
        org.springframework.security.oauth2.jwt.JwtTimestampValidator timestampValidator = new org.springframework.security.oauth2.jwt.JwtTimestampValidator();
        org.springframework.security.oauth2.jwt.JwtIssuerValidator issuerValidator = new org.springframework.security.oauth2.jwt.JwtIssuerValidator(jwtProperties.issuer());

        decoder.setJwtValidator(jwt -> {
            var timestampResult = timestampValidator.validate(jwt);
            if (!timestampResult.getErrors().isEmpty()) {
                return timestampResult;
            }
            return issuerValidator.validate(jwt);
        });
        return decoder;
    }
}