package br.com.equilibra.auth.application;

/**
 * Comando de aplicação para cadastro de usuário.
 */
public record RegisterUserCommand(String email, CharSequence password) {

    @Override
    public String toString() {
        return "RegisterUserCommand{" +
            "email='" + email + '\'' +
            ", password=<redacted>" +
            '}';
    }
}
