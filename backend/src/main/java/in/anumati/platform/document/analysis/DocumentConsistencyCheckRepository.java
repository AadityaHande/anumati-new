package in.anumati.platform.document.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentConsistencyCheckRepository extends JpaRepository<DocumentConsistencyCheck, UUID> {
    List<DocumentConsistencyCheck> findByDocument_IdOrderByFieldNameAsc(UUID documentId);
}
