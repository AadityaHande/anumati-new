package in.anumati.platform.business;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessProfileVersionRepository extends JpaRepository<BusinessProfileVersion, UUID> {
    List<BusinessProfileVersion> findByBusinessProfileIdOrderByVersionNumberDesc(UUID businessProfileId);
    boolean existsByBusinessProfileIdAndVersionNumber(UUID businessProfileId, long versionNumber);
}
