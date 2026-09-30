package in.anumati.platform.evidence;

import java.util.List;
import java.util.UUID;

public record EvidencePassportResponse(UUID businessProfileId,long profileVersion,int verifiedFields,int supportedFields,List<Entry> entries){
 public record Entry(UUID id,String fieldName,String fieldValue,String status,Long profileVersion,UUID sourceDocumentId,String sourceDocumentName,String verifiedBy,String verifiedAt){}
}
