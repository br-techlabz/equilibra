package br.com.equilibra.auth.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Converte JWT validado em Authentication com principal mínimo contendo userId.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(jwt.getSubject());
        return UsernamePasswordAuthenticationToken.authenticated(
            principal,
            jwt,
            AuthorityUtils.NO_AUTHORITIES
        );
    }
}
