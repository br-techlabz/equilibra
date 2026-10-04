package br.com.equilibra.auth.application;

/**
 * Resultado do login com access token JWT.
 */
public record LoginTokenResponse(String accessToken, String tokenType, long expiresIn) {
}
