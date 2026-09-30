package in.anumati.platform.document;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByObjectKey(String objectKey);
    List<Document> findByBusinessProfile_IdOrderByCreatedAtDesc(UUID businessProfileId);
    List<Document> findByBusinessProfile_IdAndNormalizedCategoryIn(UUID businessProfileId, java.util.Collection<String> categories);
}
