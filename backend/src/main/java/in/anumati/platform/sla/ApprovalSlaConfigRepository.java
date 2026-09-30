package in.anumati.platform.sla;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalSlaConfigRepository extends JpaRepository<ApprovalSlaConfig, UUID> {
    Optional<ApprovalSlaConfig> findByApproval_Id(UUID approvalId);
    List<ApprovalSlaConfig> findByApproval_IdIn(Collection<UUID> approvalIds);
    List<ApprovalSlaConfig> findByActiveTrue();
}
