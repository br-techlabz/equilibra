package br.com.equilibra.auth.infrastructure.security;

/**
 * Principal autenticado mínimo exposto no SecurityContext.
 */
public record AuthenticatedPrincipal(String userId) {
}
