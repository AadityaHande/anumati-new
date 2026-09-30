package in.anumati.platform.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ApplicationQueryResponseRepository extends JpaRepository<ApplicationQueryResponse, UUID> {
    List<ApplicationQueryResponse> findByQuery_IdOrderByCreatedAtAsc(UUID queryId);
}
