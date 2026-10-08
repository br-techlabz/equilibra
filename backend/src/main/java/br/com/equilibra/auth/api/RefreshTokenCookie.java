package br.com.equilibra.auth.api;
import org.springframework.http.ResponseCookie;
import java.time.Duration;
public final class RefreshTokenCookie{public static final String NAME="equilibra_refresh";private RefreshTokenCookie(){}public static ResponseCookie issue(String value,Duration maxAge,boolean secure){return ResponseCookie.from(NAME,value).httpOnly(true).secure(secure).sameSite("Lax").path("/api/auth").maxAge(maxAge).build();}public static ResponseCookie clear(boolean secure){return ResponseCookie.from(NAME,"").httpOnly(true).secure(secure).sameSite("Lax").path("/api/auth").maxAge(Duration.ZERO).build();}}
