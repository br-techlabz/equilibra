package br.com.equilibra.auth.application;

import br.com.equilibra.auth.domain.validator.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Serviço de aplicação para operações de senha.
 * <p>
 * Centraliza o uso do {@link PasswordEncoder} e da {@link PasswordValidator},
 * evitando espelhar chamadas diretas ao encoder por todo o sistema.
 * </p>
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Validar senha contra a política antes de codificar</li>
 *   <li>Gerar hash seguro via PasswordEncoder</li>
 *   <li>Verificar senha contra hash armazenado</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Não</strong> implementa fluxo de autenticação, login, JWT ou recuperação de senha.
 * </p>
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Codifica uma senha válida gerando seu hash seguro.
     *
     * @param rawPassword senha em texto puro (já validada pelo chamador ou será validada aqui)
     * @return hash BCrypt da senha
     * @throws IllegalArgumentException se a senha violar a política
     */
    public String encode(CharSequence rawPassword) {
        PasswordValidator.validateOrThrow(rawPassword);
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifica se a senha fornecida corresponde ao hash armazenado.
     * <p>
     * Utiliza exclusivamente {@link PasswordEncoder#matches(CharSequence, String)},
     * nunca comparação direta ou decodificação de hash.
     * </p>
     *
     * @param rawPassword     senha em texto puro fornecida pelo usuário
     * @param encodedPassword hash armazenado no banco (passwordHash)
     * @return true se a senha confere, false caso contrário
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Verifica se o hash armazenado precisa ser atualizado (re-encoded) com parâmetros atuais.
     * <p>
     * Útil para migração gradual de hashes antigos após login bem-sucedido.
     * </p>
     *
     * @param encodedPassword hash armazenado
     * @return true se o hash deveria ser atualizado
     */
    public boolean upgradeEncoding(String encodedPassword) {
        return passwordEncoder.upgradeEncoding(encodedPassword);
    }
}