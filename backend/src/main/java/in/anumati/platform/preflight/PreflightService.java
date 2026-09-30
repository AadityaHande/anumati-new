package in.anumati.platform.preflight;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.anumati.platform.analysis.AnalysisRun;
import in.anumati.platform.analysis.AnalysisRunRepository;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.dependency.ApprovalDependency;
import in.anumati.platform.dependency.ApprovalDependencyRepository;
import in.anumati.platform.dependency.DependencyType;
import in.anumati.platform.readiness.DocumentReadinessService;
import in.anumati.platform.readiness.ReadinessResponse;
import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.application.ApplicationStatus;
import in.anumati.platform.evidence.VerifiedEvidenceRepository;
import in.anumati.platform.regulatory.ApplicabilityStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PreflightService {
    private final BusinessProfileService profiles;
    private final AnalysisRunRepository analyses;
    private final DocumentReadinessService readiness;
    private final ApprovalDependencyRepository dependencies;
    private final ApplicationRepository applications;
    private final VerifiedEvidenceRepository evidence;
    private final ObjectMapper mapper;

    public PreflightService(BusinessProfileService profiles, AnalysisRunRepository analyses, DocumentReadinessService readiness, ApprovalDependencyRepository dependencies, ApplicationRepository applications, VerifiedEvidenceRepository evidence, ObjectMapper mapper) {
        this.profiles=profiles; this.analyses=analyses; this.readiness=readiness; this.dependencies=dependencies; this.applications=applications; this.evidence=evidence; this.mapper=mapper;
    }

    @Transactional(readOnly = true)
    public PreflightResponse evaluate(UUID profileId, UUID analysisRunId, String actor) {
        var profile=profiles.getForActor(profileId, actor);
        AnalysisRun run=analyses.findById(analysisRunId).orElseThrow(() -> new IllegalArgumentException("Analysis run not found"));
        if(!run.getBusinessProfileId().equals(profileId)) throw new IllegalArgumentException("Analysis run does not belong to this profile");
        ReadinessResponse ready=readiness.evaluate(profileId, analysisRunId, actor);
        Map<UUID, Map<String,Object>> resultByApproval = new HashMap<>();
        List<Map<String,Object>> raw=mapper.convertValue(run.getResultSnapshot().getOrDefault("results", List.of()), new TypeReference<>() {});
        for(var item:raw) resultByApproval.put(UUID.fromString(String.valueOf(item.get("approvalId"))), item);
        Map<UUID, ReadinessResponse.ApprovalReadiness> readinessByApproval=ready.approvals().stream().collect(Collectors.toMap(ReadinessResponse.ApprovalReadiness::approvalId, x->x, (a,b)->a));
        Map<UUID,List<ApprovalDependency>> blockers=dependencies.findByActiveTrue().stream().filter(d->d.getDependencyType()==DependencyType.BLOCKING).collect(Collectors.groupingBy(d->d.getApproval().getId()));
        List<PreflightResponse.Check> checks=new ArrayList<>();
        int blockersCount=0, warnings=0;
        if(ready.knownRequirements()==0 && !ready.approvals().isEmpty()) {
            checks.add(new PreflightResponse.Check("DOCUMENT_REQUIREMENTS_NOT_CONFIGURED","BLOCKER","Document requirements are not configured","The current regulatory catalogue does not contain verified document requirements for the applicable approvals."));
            blockersCount++;
        } else if(ready.overallScore()<100){
            checks.add(new PreflightResponse.Check("DOCUMENT_READINESS","BLOCKER","Documents are not fully ready","Complete mandatory document requirements before submission."));
            blockersCount++;
        }
        if(evidence.findByBusinessProfile_IdAndProfileVersionAndStatusOrderByFieldName(profileId, profile.getVersionNumber(), in.anumati.platform.evidence.VerifiedEvidenceStatus.VERIFIED).isEmpty()){checks.add(new PreflightResponse.Check("VERIFIED_EVIDENCE","WARNING","No verified evidence has been promoted","Promote matched identity and registration fields into the evidence passport where available.")); warnings++;}
        List<PreflightResponse.ApprovalCheck> approvalChecks=new ArrayList<>();
        for(var item:ready.approvals()){
            Map<String,Object> rawResult=resultByApproval.get(item.approvalId());
            if(rawResult==null) continue;
            List<String> localBlockers=new ArrayList<>();
            if("NOT_CONFIGURED".equals(item.status())) localBlockers.add("Verified document requirements are not configured");
            else if(!"READY".equals(item.status())) localBlockers.add("Mandatory document readiness is not complete");
            for(var d:blockers.getOrDefault(item.approvalId(),List.of())){
                Map<String,Object> depResult=resultByApproval.get(d.getDependsOnApproval().getId());
                if(depResult!=null && !ApplicabilityStatus.NOT_APPLICABLE.name().equals(String.valueOf(depResult.get("status")))){
                    var depReadiness=readinessByApproval.get(d.getDependsOnApproval().getId());
                    if(depReadiness==null || !"READY".equals(depReadiness.status())) localBlockers.add("Blocking dependency: "+d.getDependsOnApproval().getCode());
                }
            }
            boolean activeApplication=applications.findByBusinessProfile_IdOrderByCreatedAtDesc(profileId).stream().anyMatch(a->a.getApproval().getId().equals(item.approvalId())
                    && a.getStatus()!=ApplicationStatus.REJECTED
                    && a.getStatus()!=ApplicationStatus.APPROVED
                    && a.getStatus()!=ApplicationStatus.RENEWAL_DUE);
            if(activeApplication) localBlockers.add("An active application already exists");
            if(!localBlockers.isEmpty()){blockersCount++;}
            approvalChecks.add(new PreflightResponse.ApprovalCheck(item.approvalId(),item.approvalCode(),item.approvalName(),item.status(),!blockers.getOrDefault(item.approvalId(),List.of()).isEmpty(),localBlockers));
        }
        String status=blockersCount==0?"READY_TO_SUBMIT":"ACTION_REQUIRED";
        return new PreflightResponse(profileId,analysisRunId,profile.getVersionNumber(),status,blockersCount,warnings,checks,approvalChecks);
    }
}
