package br.com.equilibra.auth.application;

/**
 * Identidade autenticada interna mínima.
 */
public record AuthenticatedUser(String userId, String email) {
}
