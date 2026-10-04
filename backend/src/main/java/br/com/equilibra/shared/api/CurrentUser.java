package br.com.equilibra.shared.api;

import java.util.UUID;

/**
 * Abstração central para obtenção da identidade do usuário autenticado.
 * <p>
 * O objetivo é impedir que código de aplicação precise interpretar diretamente
 * o {@link org.springframework.security.core.context.SecurityContextHolder},
 * JWT, claims ou strings.
 * </p>
 *
 * @see SpringSecurityCurrentUser
 */
public interface CurrentUser {

    /**
     * Retorna o UUID do usuário autenticado.
     *
     * @return UUID do usuário autenticado
     * @throws IllegalStateException se não houver autenticação válida
     * @throws IllegalArgumentException se a identidade autenticada não puder ser convertida para UUID
     */
    UUID id();
}