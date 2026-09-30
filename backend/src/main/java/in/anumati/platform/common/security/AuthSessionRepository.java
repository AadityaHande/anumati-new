package in.anumati.platform.common.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByTokenHash(String tokenHash);
    long deleteByTokenHash(String tokenHash);
    long deleteByExpiresAtBefore(Instant instant);
}
