package in.anumati.platform.readiness;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.anumati.platform.analysis.AnalysisRun;
import in.anumati.platform.analysis.AnalysisRunRepository;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.document.Document;
import in.anumati.platform.document.DocumentRepository;
import in.anumati.platform.document.DocumentStatus;
import in.anumati.platform.regulatory.ApplicabilityStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentReadinessService {
    private final BusinessProfileService profileService;
    private final AnalysisRunRepository analysisRunRepository;
    private final DocumentRequirementRepository requirementRepository;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public DocumentReadinessService(BusinessProfileService profileService,
                                    AnalysisRunRepository analysisRunRepository,
                                    DocumentRequirementRepository requirementRepository,
                                    DocumentRepository documentRepository,
                                    ObjectMapper objectMapper) {
        this.profileService = profileService;
        this.analysisRunRepository = analysisRunRepository;
        this.requirementRepository = requirementRepository;
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ReadinessResponse evaluate(UUID businessProfileId, UUID analysisRunId, String actor) {
        profileService.getForActor(businessProfileId, actor);
        AnalysisRun run = analysisRunRepository.findById(analysisRunId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis run not found: " + analysisRunId));
        if (!run.getBusinessProfileId().equals(businessProfileId)) {
            throw new IllegalArgumentException("Analysis run does not belong to the requested business profile");
        }

        List<Map<String, Object>> results = objectMapper.convertValue(
                run.getResultSnapshot().getOrDefault("results", List.of()),
                new TypeReference<>() {});

        Set<String> applicableApprovalCodes = results.stream()
                .filter(this::requiresPreparation)
                .map(item -> String.valueOf(item.get("approvalCode")))
                .collect(Collectors.toSet());

        Map<String, List<Document>> documentsByCategory = documentRepository
                .findByBusinessProfile_IdOrderByCreatedAtDesc(businessProfileId).stream()
                .filter(document -> isUsableDocument(document, run.getProfileVersion()))
                .collect(Collectors.groupingBy(Document::getNormalizedCategory));

        List<DocumentRequirement> requirements = requirementRepository.findByActiveTrue();
        Map<String, List<DocumentRequirement>> requirementsByApproval = requirements.stream()
                .filter(r -> applicableApprovalCodes.contains(r.getApproval().getCode()))
                .collect(Collectors.groupingBy(r -> r.getApproval().getCode(), LinkedHashMap::new, Collectors.toList()));

        List<ReadinessResponse.ApprovalReadiness> approvalReadiness = new ArrayList<>();
        int totalKnown = 0;
        int totalSatisfied = 0;

        for (Map<String, Object> result : results) {
            if (!requiresPreparation(result)) {
                continue;
            }

            String code = String.valueOf(result.get("approvalCode"));
            List<DocumentRequirement> approvalRequirements = requirementsByApproval.getOrDefault(code, List.of());
            int mandatoryKnown = 0;
            int mandatorySatisfied = 0;
            int satisfied = 0;
            List<ReadinessResponse.RequirementStatus> statuses = new ArrayList<>();

            for (DocumentRequirement requirement : approvalRequirements) {
                totalKnown++;
                if (requirement.isMandatory()) {
                    mandatoryKnown++;
                }

                List<Document> matches = documentsByCategory.getOrDefault(requirement.getCategory(), List.of());
                boolean satisfiedRequirement = matches.stream().anyMatch(document -> document.getStatus() == DocumentStatus.READY);
                if (satisfiedRequirement) {
                    satisfied++;
                    totalSatisfied++;
                    if (requirement.isMandatory()) {
                        mandatorySatisfied++;
                    }
                }

                statuses.add(new ReadinessResponse.RequirementStatus(
                        requirement.getId(),
                        requirement.getCategory(),
                        requirement.getDocumentName(),
                        requirement.isMandatory(),
                        satisfiedRequirement,
                        satisfiedRequirement ? "READY" : "MISSING",
                        matches.stream().map(Document::getId).toList()));
            }

            int score = approvalRequirements.isEmpty()
                    ? 0
                    : (int) Math.round((satisfied * 100.0) / approvalRequirements.size());
            String status = approvalRequirements.isEmpty()
                    ? "NOT_CONFIGURED"
                    : (mandatorySatisfied == mandatoryKnown ? "READY" : "ACTION_NEEDED");

            approvalReadiness.add(new ReadinessResponse.ApprovalReadiness(
                    parseUuid(result.get("approvalId")),
                    code,
                    String.valueOf(result.get("approvalName")),
                    status,
                    score,
                    mandatoryKnown,
                    mandatorySatisfied,
                    statuses));
        }

        int overallScore = totalKnown == 0
                ? 0
                : (int) Math.round((totalSatisfied * 100.0) / totalKnown);

        return new ReadinessResponse(
                businessProfileId,
                analysisRunId,
                overallScore,
                totalKnown,
                totalSatisfied,
                approvalReadiness);
    }

    private boolean isUsableDocument(Document document, long profileVersion) {
        return document.getStatus() == DocumentStatus.UPLOADED
                || document.getStatus() == DocumentStatus.NEEDS_REVIEW
                || (document.getStatus() == DocumentStatus.READY
                    && java.util.Objects.equals(document.getValidatedProfileVersion(), profileVersion));
    }

    private boolean requiresPreparation(Map<String, Object> result) {
        Object status = result.get("status");
        return ApplicabilityStatus.APPLICABLE.name().equals(status)
                || ApplicabilityStatus.CONDITIONAL.name().equals(status);
    }

    private UUID parseUuid(Object value) {
        return value == null ? null : UUID.fromString(String.valueOf(value));
    }
}
