package in.anumati.platform.application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.Collection;

public interface ApplicationRepository extends JpaRepository<ApplicationRecord, UUID> {
    List<ApplicationRecord> findByBusinessProfile_IdOrderByCreatedAtDesc(UUID businessProfileId);
    List<ApplicationRecord> findAllByOrderByCreatedAtDesc();
    List<ApplicationRecord> findByStatusIn(Collection<ApplicationStatus> statuses);

    boolean existsByBusinessProfile_IdAndApproval_IdAndAnalysisRunIdAndStatusNotIn(UUID businessProfileId, UUID approvalId, UUID analysisRunId, List<ApplicationStatus> excluded);
}
