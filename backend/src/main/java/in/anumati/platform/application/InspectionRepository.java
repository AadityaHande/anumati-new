package in.anumati.platform.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InspectionRepository extends JpaRepository<Inspection, UUID> {
    List<Inspection> findByApplication_IdOrderByScheduledAtAsc(UUID applicationId);
    List<Inspection> findAllByOrderByScheduledAtAsc();
}
