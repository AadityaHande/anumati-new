package in.anumati.platform.dependency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalDependencyRepository extends JpaRepository<ApprovalDependency, UUID> {
    List<ApprovalDependency> findByActiveTrue();
    List<ApprovalDependency> findByApproval_IdAndActiveTrue(UUID approvalId);
    List<ApprovalDependency> findByDependsOnApproval_IdAndActiveTrue(UUID approvalId);
}
