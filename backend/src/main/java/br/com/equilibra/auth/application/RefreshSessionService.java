package br.com.equilibra.auth.application;

import br.com.equilibra.auth.domain.RefreshSession;
import br.com.equilibra.auth.infrastructure.RefreshSessionRepository;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;

@Service
public class RefreshSessionService {
 private final RefreshSessionRepository sessions; private final UserRepository users; private final Clock clock; private final SecureRandom random=new SecureRandom(); private final Duration ttl;
 public RefreshSessionService(RefreshSessionRepository sessions,UserRepository users,Clock clock,@Value("${equilibra.security.refresh-token-ttl:30d}")Duration ttl){this.sessions=sessions;this.users=users;this.clock=clock;this.ttl=ttl;}
 @Transactional public String issue(AuthenticatedUser user){String raw=generate();Instant now=Instant.now(clock);sessions.save(new RefreshSession(user.userId(),hash(raw),now.plus(ttl),now));return raw;}
 @Transactional public AuthenticatedUser rotate(String raw){if(raw==null||raw.isBlank())throw new IllegalArgumentException("Invalid refresh session.");Instant now=Instant.now(clock);RefreshSession session=sessions.findByTokenHash(hash(raw)).filter(s->s.usableAt(now)).orElseThrow(()->new IllegalArgumentException("Invalid refresh session."));session.rotate(now);sessions.save(session);var user=users.findById(session.getOwnerId()).filter(u->u.isActive()).orElseThrow(()->new IllegalArgumentException("Invalid refresh session."));return new AuthenticatedUser(user.getId(),user.getEmail());}
 @Transactional public void revoke(String raw){if(raw==null||raw.isBlank())return;sessions.findByTokenHash(hash(raw)).ifPresent(s->{s.rotate(Instant.now(clock));sessions.save(s);});}
 private String generate(){byte[] bytes=new byte[48];random.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
 private static String hash(String value){try{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
