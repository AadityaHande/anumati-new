package in.anumati.platform.evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VerifiedEvidenceRepository extends JpaRepository<VerifiedEvidence, UUID> {
 List<VerifiedEvidence> findByBusinessProfile_IdAndProfileVersionAndStatusOrderByFieldName(UUID profileId,Long profileVersion,VerifiedEvidenceStatus status);
 Optional<VerifiedEvidence> findByBusinessProfile_IdAndFieldNameAndProfileVersion(UUID profileId,String fieldName,Long profileVersion);
}
