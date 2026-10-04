package br.com.equilibra.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Resposta segura para cadastro de usuário.
 */
@Schema(description = "Usuário cadastrado")
public record RegisteredUserResponse(
    @Schema(description = "Identificador do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,

    @Schema(description = "Email normalizado do usuário", example = "user@example.com")
    String email,

    @Schema(description = "Data/hora de criação da conta")
    Instant createdAt
) {
}
