package br.com.equilibra.shared.api;

import br.com.equilibra.auth.infrastructure.security.AuthenticatedPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementação de {@link CurrentUser} baseada no Spring Security.
 * <p>
 * Responsabilidades:
 * <ol>
 *   <li>Acessar a {@link Authentication} atual do {@link SecurityContextHolder}</li>
 *   <li>Confirmar que a autenticação está presente e não é anônima</li>
 *   <li>Obter o principal tipado ({@link AuthenticatedPrincipal})</li>
 *   <li>Converter o ID para {@link UUID}</li>
 *   <li>Retornar a identidade ou falhar explicitamente se inválida</li>
 * </ol>
 * <p>
 * Não consulta banco de dados. A validação de existência/atividade do usuário
 * é responsabilidade de cada caso de uso quando apropriado (ex: endpoint {@code /me}).
 * </p>
 */
@Component
public class SpringSecurityCurrentUser implements CurrentUser {

    @Override
    public UUID id() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authentication found in SecurityContext");
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Authentication is anonymous");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedPrincipal authenticatedPrincipal)) {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }

        String userId = authenticatedPrincipal.userId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("AuthenticatedPrincipal.userId is blank");
        }

        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Authenticated userId is not a valid UUID: " + userId, e);
        }
    }
}