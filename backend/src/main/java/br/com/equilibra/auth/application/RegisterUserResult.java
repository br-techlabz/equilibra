package br.com.equilibra.auth.application;

import java.time.Instant;

/**
 * Resultado seguro do cadastro de usuário.
 */
public record RegisterUserResult(String id, String email, Instant createdAt) {
}
