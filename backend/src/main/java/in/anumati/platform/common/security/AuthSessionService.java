package in.anumati.platform.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthSessionService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthSessionRepository repository;
    private final String cookieName;
    private final Duration lifetime;
    private final boolean secureCookie;
    private final String cookieDomain;

    public AuthSessionService(
            AuthSessionRepository repository,
            @Value("${anumati.session.cookie-name:ANUMATI_SESSION}") String cookieName,
            @Value("${anumati.session.lifetime-hours:8}") long lifetimeHours,
            @Value("${anumati.session.secure-cookie:false}") boolean secureCookie,
            @Value("${anumati.session.cookie-domain:}") String cookieDomain) {
        this.repository = repository;
        this.cookieName = cookieName;
        this.lifetime = Duration.ofHours(Math.max(1, lifetimeHours));
        this.secureCookie = secureCookie;
        this.cookieDomain = cookieDomain == null ? "" : cookieDomain.trim();
    }

    @Transactional
    public IssuedSession create(String username) {
        repository.deleteByExpiresAtBefore(Instant.now());
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = Instant.now();
        AuthSession session = repository.save(new AuthSession(hash(rawToken), username, now, now.plus(lifetime)));
        return new IssuedSession(rawToken, session.getUsername(), session.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public AuthSession findActive(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return null;
        return repository.findByTokenHash(hash(rawToken))
                .filter(session -> session.getExpiresAt().isAfter(Instant.now()))
                .orElse(null);
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) repository.deleteByTokenHash(hash(rawToken));
    }

    public String cookieName() { return cookieName; }

    public ResponseCookie sessionCookie(String rawToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, rawToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(lifetime);
        if (!cookieDomain.isBlank()) builder.domain(cookieDomain);
        return builder.build();
    }

    public ResponseCookie clearCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO);
        if (!cookieDomain.isBlank()) builder.domain(cookieDomain);
        return builder.build();
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Hex.hex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public record IssuedSession(String rawToken, String username, Instant expiresAt) {}

    private static final class Hex {
        private static final char[] HEX = "0123456789abcdef".toCharArray();
        private static String hex(byte[] bytes) {
            char[] result = new char[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++) {
                int value = bytes[i] & 0xff;
                result[i * 2] = HEX[value >>> 4];
                result[i * 2 + 1] = HEX[value & 0x0f];
            }
            return new String(result);
        }
    }
}
