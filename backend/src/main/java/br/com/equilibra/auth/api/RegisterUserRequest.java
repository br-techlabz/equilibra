package br.com.equilibra.auth.api;

import br.com.equilibra.auth.api.validation.ValidPassword;
import br.com.equilibra.auth.api.validation.ValidRegistrationEmail;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Payload de cadastro de usuário.
 */
@Schema(description = "Dados para criação de uma nova conta de usuário")
public record RegisterUserRequest(
    @Schema(description = "Email do usuário", example = "user@example.com", maxLength = 255)
    @ValidRegistrationEmail
    String email,

    @Schema(description = "Senha do usuário", example = "senha ficticia segura", minLength = 8, maxLength = 128)
    @ValidPassword
    String password
) {

    @Override
    public String toString() {
        return "RegisterUserRequest{" +
            "email='" + email + '\'' +
            ", password=<redacted>" +
            '}';
    }
}
