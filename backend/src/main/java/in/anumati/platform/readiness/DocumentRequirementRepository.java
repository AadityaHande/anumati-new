package in.anumati.platform.readiness;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, UUID> {
    List<DocumentRequirement> findByActiveTrue();
    List<DocumentRequirement> findByApproval_IdAndActiveTrue(UUID approvalId);
}
