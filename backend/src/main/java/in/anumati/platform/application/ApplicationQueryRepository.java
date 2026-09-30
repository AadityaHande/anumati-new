package in.anumati.platform.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ApplicationQueryRepository extends JpaRepository<ApplicationQuery, UUID> {
    List<ApplicationQuery> findByApplication_IdOrderByRaisedAtAsc(UUID applicationId);
}
