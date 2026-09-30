package in.anumati.platform.evidence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.document.Document;
import in.anumati.platform.document.DocumentRepository;
import in.anumati.platform.document.analysis.DocumentConsistencyCheck;
import in.anumati.platform.document.analysis.DocumentConsistencyCheckRepository;
import in.anumati.platform.document.analysis.DocumentAnalysisStatus;
import in.anumati.platform.document.analysis.DocumentExtraction;
import in.anumati.platform.document.analysis.DocumentExtractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class VerifiedEvidenceService {
 private final VerifiedEvidenceRepository repo; private final BusinessProfileService profiles; private final DocumentRepository docs; private final DocumentExtractionRepository extractions; private final DocumentConsistencyCheckRepository checks; private final AuditService audit; private final ObjectMapper mapper;
 public VerifiedEvidenceService(VerifiedEvidenceRepository r,BusinessProfileService p,DocumentRepository d,DocumentExtractionRepository e,DocumentConsistencyCheckRepository c,AuditService a,ObjectMapper m){repo=r;profiles=p;docs=d;extractions=e;checks=c;audit=a;mapper=m;}
 @Transactional public VerifiedEvidenceResponse promote(UUID profileId,UUID documentId,String actor){BusinessProfile p=profiles.getForActor(profileId,actor);Document d=docs.findById(documentId).orElseThrow(()->new IllegalArgumentException("Document not found: "+documentId)); if(!d.getBusinessProfile().getId().equals(profileId)) throw new IllegalArgumentException("Document does not belong to the business profile"); if(d.getStatus()!=in.anumati.platform.document.DocumentStatus.READY) throw new IllegalArgumentException("Only a READY document can become verified evidence"); if(!java.util.Objects.equals(d.getValidatedProfileVersion(), p.getVersionNumber())) throw new IllegalArgumentException("Document readiness was evaluated against an older business profile version; re-analyse the document"); DocumentExtraction ex=extractions.findByDocument_Id(documentId).orElseThrow(()->new IllegalArgumentException("Document has not been analysed")); if(ex.getStatus()!=DocumentAnalysisStatus.COMPLETED) throw new IllegalArgumentException("Document analysis is not complete"); List<DocumentConsistencyCheck> cc=checks.findByDocument_IdOrderByFieldNameAsc(documentId); if(cc.isEmpty()||cc.stream().anyMatch(c->c.getStatus()!=in.anumati.platform.document.analysis.ConsistencyStatus.MATCH)) throw new IllegalArgumentException("All available consistency checks must match before evidence can be verified"); Map<String,String> fields; try{fields=mapper.readValue(ex.getExtractedFieldsJson(),new TypeReference<>(){});}catch(Exception e){throw new IllegalArgumentException("Extracted fields could not be read");}
 String[] candidates={"BUSINESS_NAME","DISTRICT","PAN","GSTIN"}; VerifiedEvidenceResponse last=null; for(String field:candidates){String value=fields.get(field); if(value==null||value.isBlank()) continue; VerifiedEvidence ve=repo.findByBusinessProfile_IdAndFieldNameAndProfileVersion(profileId,field,p.getVersionNumber()).map(existing->existing).orElse(null); if(ve==null){ve=repo.save(new VerifiedEvidence(p,field,value,d,actor));} else {throw new IllegalArgumentException("Verified evidence already exists for "+field+"; revoke or update it explicitly before replacing it");} audit.record(actor,"VERIFIED_EVIDENCE_CREATED","VerifiedEvidence",ve.getId(),Map.of("businessProfileId",profileId,"documentId",documentId,"field",field)); last=VerifiedEvidenceResponse.from(ve);} if(last==null) throw new IllegalArgumentException("The document did not contain a supported verifiable field"); return last; }
 @Transactional(readOnly=true) public EvidencePassportResponse passport(UUID profileId,String actor){
   BusinessProfile p=profiles.getForActor(profileId,actor);
   var current=repo.findByBusinessProfile_IdAndProfileVersionAndStatusOrderByFieldName(profileId,p.getVersionNumber(),VerifiedEvidenceStatus.VERIFIED);
   var entries=current.stream().map(e->new EvidencePassportResponse.Entry(e.getId(),e.getFieldName(),e.getFieldValue(),e.getStatus().name(),e.getProfileVersion(),e.getSourceDocument().getId(),e.getSourceDocument().getOriginalFilename(),e.getVerifiedBy(),e.getVerifiedAt().toString())).toList();
   int supported=0; for(String x: List.of("BUSINESS_NAME","DISTRICT","PAN","GSTIN")){ if(current.stream().anyMatch(e->e.getFieldName().equals(x))) supported++; }
   return new EvidencePassportResponse(profileId,p.getVersionNumber(),current.size(),supported,entries);
 }

 @Transactional(readOnly=true) public List<VerifiedEvidenceResponse> list(UUID profileId,String actor){BusinessProfile p=profiles.getForActor(profileId,actor); return repo.findByBusinessProfile_IdAndProfileVersionAndStatusOrderByFieldName(profileId, p.getVersionNumber(), VerifiedEvidenceStatus.VERIFIED).stream().map(VerifiedEvidenceResponse::from).toList();}
}
