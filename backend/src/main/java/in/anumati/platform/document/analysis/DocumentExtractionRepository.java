package in.anumati.platform.document.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {
    Optional<DocumentExtraction> findByDocument_Id(UUID documentId);
}
