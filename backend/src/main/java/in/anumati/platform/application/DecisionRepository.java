package in.anumati.platform.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {
    Optional<Decision> findByApplication_Id(UUID applicationId);
}
