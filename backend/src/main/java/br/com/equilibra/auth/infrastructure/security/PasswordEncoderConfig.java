package br.com.equilibra.auth.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração do PasswordEncoder.
 * <p>
 * Utiliza BCrypt como algoritmo padrão com cost factor 10 (padrão do Spring Security).
 * O BCrypt já gerencia salt internamente e é resistente a ataques de força bruta
 * através de seu fator de trabalho configurável.
 * </p>
 * <p>
 * Para evolução futura, pode-se migrar para {@code DelegatingPasswordEncoder}
 * com prefixo {@code {bcrypt}} permitindo suporte a múltiplos algoritmos
 * (Argon2, PBKDF2) sem redefinição imediata de senhas existentes.
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}