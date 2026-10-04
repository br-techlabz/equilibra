package br.com.equilibra.auth.api;

import br.com.equilibra.auth.api.validation.ValidRegistrationEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload de autenticação por email e senha.
 */
@Schema(description = "Credenciais para autenticação por email e senha")
public record LoginRequest(
    @Schema(description = "Email do usuário", example = "user@example.com", maxLength = 255)
    @ValidRegistrationEmail
    String email,

    @Schema(description = "Senha do usuário", example = "senha ficticia segura")
    @NotBlank(message = "Password is required")
    String password
) {

    @Override
    public String toString() {
        return "LoginRequest{" +
            "email='" + email + '\'' +
            ", password=<redacted>" +
            '}';
    }
}
