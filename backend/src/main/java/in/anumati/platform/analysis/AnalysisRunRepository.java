package in.anumati.platform.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, UUID> {
    java.util.Optional<AnalysisRun> findFirstByBusinessProfileIdOrderByEvaluatedAtDesc(UUID businessProfileId);
}
