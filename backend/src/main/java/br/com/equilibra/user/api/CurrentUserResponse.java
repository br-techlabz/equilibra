package br.com.equilibra.user.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Resposta do endpoint {@code GET /api/users/me} representando o usuário autenticado.
 */
@Schema(description = "Informações do usuário autenticado")
public record CurrentUserResponse(

    @Schema(description = "ID único do usuário (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,

    @Schema(description = "Email do usuário", example = "usuario@exemplo.com")
    String email,

    @Schema(description = "Data de criação da conta")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant createdAt
) {
}