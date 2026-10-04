package br.com.equilibra.auth.application;

import br.com.equilibra.auth.infrastructure.jwt.JwtProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Serviço responsável exclusivamente pela emissão de access tokens JWT.
 */
@Service
public class JwtTokenService {

    public static final String TOKEN_TYPE = "Bearer";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    /**
     * Gera um access token JWT para a identidade autenticada.
     *
     * @param authenticatedUser identidade autenticada
     * @return resposta de login com token e metadados públicos
     */
    public LoginTokenResponse issueAccessToken(AuthenticatedUser authenticatedUser) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .subject(authenticatedUser.userId())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .id(UUID.randomUUID().toString())
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginTokenResponse(token, TOKEN_TYPE, jwtProperties.accessTokenTtl().toSeconds());
    }
}
