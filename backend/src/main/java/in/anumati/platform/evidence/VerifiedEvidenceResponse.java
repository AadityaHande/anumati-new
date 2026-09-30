package in.anumati.platform.evidence;
import java.time.Instant; import java.util.UUID;
public record VerifiedEvidenceResponse(UUID id,String fieldName,String fieldValue,UUID sourceDocumentId,String sourceDocumentName,String verifiedBy,Instant verifiedAt,Long profileVersion,String status){
 public static VerifiedEvidenceResponse from(VerifiedEvidence e){return new VerifiedEvidenceResponse(e.getId(),e.getFieldName(),e.getFieldValue(),e.getSourceDocument().getId(),e.getSourceDocument().getOriginalFilename(),e.getVerifiedBy(),e.getVerifiedAt(),e.getProfileVersion(),e.getStatus().name());}
}
