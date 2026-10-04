package br.com.equilibra.auth.domain.policy;

/**
 * Política de senha do Equilibra.
 * <p>
 * Define os requisitos mínimos e máximos para senhas válidas.
 * A política é intencionalmente simples, priorizando comprimento
 * e permitindo passphrases, sem regras artificiais de complexidade
 * (maiúscula, minúscula, número, símbolo obrigatórios).
 * </p>
 * <p>
 * <strong>Regras:</strong>
 * <ul>
 *   <li>Mínimo: 8 caracteres</li>
 *   <li>Máximo: 128 caracteres</li>
 *   <li>Não pode ser vazia</li>
 *   <li>Não pode ser composta apenas por espaços em branco</li>
 *   <li>Espaços <strong>não</strong> são removidos silenciosamente (trim)</li>
 *   <li>Unicode preservado integralmente</li>
 * </ul>
 * </p>
 */
public final class PasswordPolicy {

    /**
     * Comprimento mínimo aceito para senha.
     * Valor: 8 caracteres.
     */
    public static final int MIN_LENGTH = 8;

    /**
     * Comprimento máximo aceito para senha.
     * Valor: 128 caracteres (permite passphrases longas).
     */
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {
        // Classe utilitária, não instanciável
    }

    /**
     * Verifica se a senha atende ao comprimento mínimo.
     *
     * @param rawPassword senha em texto puro
     * @return true se atende ao mínimo
     */
    public static boolean meetsMinLength(CharSequence rawPassword) {
        return rawPassword != null && rawPassword.length() >= MIN_LENGTH;
    }

    /**
     * Verifica se a senha não excede o comprimento máximo.
     *
     * @param rawPassword senha em texto puro
     * @return true se não excede o máximo
     */
    public static boolean meetsMaxLength(CharSequence rawPassword) {
        return rawPassword != null && rawPassword.length() <= MAX_LENGTH;
    }

    /**
     * Verifica se a senha não é vazia ou nula.
     *
     * @param rawPassword senha em texto puro
     * @return true se não é vazia
     */
    public static boolean isNotBlank(CharSequence rawPassword) {
        return rawPassword != null && !rawPassword.isEmpty();
    }

    /**
     * Verifica se a senha não é composta apenas por espaços em branco.
     *
     * @param rawPassword senha em texto puro
     * @return true se contém pelo menos um caractere não-espaço
     */
    public static boolean isNotOnlyWhitespace(CharSequence rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        for (int i = 0; i < rawPassword.length(); i++) {
            if (!Character.isWhitespace(rawPassword.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}