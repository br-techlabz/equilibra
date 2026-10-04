package br.com.equilibra.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta do login com access token JWT.
 */
@Schema(description = "Access token para autenticação Bearer")
public record LoginResponse(
    @Schema(description = "JWT assinado para autenticação Bearer")
    String accessToken,

    @Schema(description = "Tipo do token", example = "Bearer")
    String tokenType,

    @Schema(description = "Tempo de validade do access token em segundos", example = "900")
    long expiresIn
) {
}
