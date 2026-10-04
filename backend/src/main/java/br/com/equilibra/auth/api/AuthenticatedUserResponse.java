package br.com.equilibra.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta temporária e segura para autenticação sem emissão de token.
 */
@Schema(description = "Identidade autenticada temporária. Tokens serão adicionados em tarefa futura.")
public record AuthenticatedUserResponse(
    @Schema(description = "Identificador do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,

    @Schema(description = "Email normalizado do usuário", example = "user@example.com")
    String email
) {
}
