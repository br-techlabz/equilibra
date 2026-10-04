package br.com.equilibra.auth.application;

/**
 * Falha segura de autenticação sem expor qual credencial falhou.
 */
public class InvalidCredentialsException extends RuntimeException {

    public static final String MESSAGE = "Invalid email or password.";

    public InvalidCredentialsException() {
        super(MESSAGE);
    }
}
