package br.com.equilibra.auth.application;

/**
 * Comando de aplicação para autenticação por email e senha.
 */
public record AuthenticateUserCommand(String email, CharSequence password) {

    @Override
    public String toString() {
        return "AuthenticateUserCommand{" +
            "email='" + email + '\'' +
            ", password=<redacted>" +
            '}';
    }
}
