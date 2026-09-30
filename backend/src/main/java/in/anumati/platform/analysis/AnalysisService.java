package in.anumati.platform.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.business.BusinessProfileVersionRepository;
import in.anumati.platform.regulatory.RegulatoryRule;
import in.anumati.platform.regulatory.RegulatoryRuleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalysisService {
    private final BusinessProfileService profileService;
    private final RegulatoryRuleRepository ruleRepository;
    private final RuleEngineService ruleEngine;
    private final AnalysisRunRepository analysisRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final BusinessProfileVersionRepository profileVersions;
    private final ScrutinyAssessmentService scrutinyAssessmentService;

    @Value("${anumati.analysis.require-verified-sources:true}")
    private boolean requireVerifiedSources;

    public AnalysisService(BusinessProfileService profileService,
                           RegulatoryRuleRepository ruleRepository,
                           RuleEngineService ruleEngine,
                           AnalysisRunRepository analysisRepository,
                           AuditService auditService,
                           ObjectMapper objectMapper,
                           BusinessProfileVersionRepository profileVersions,
                           ScrutinyAssessmentService scrutinyAssessmentService) {
        this.profileService = profileService;
        this.ruleRepository = ruleRepository;
        this.ruleEngine = ruleEngine;
        this.analysisRepository = analysisRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.profileVersions = profileVersions;
        this.scrutinyAssessmentService = scrutinyAssessmentService;
    }

    @Transactional
    public AnalysisRunResponse analyse(UUID businessProfileId, String actor) {
        BusinessProfile profile = profileService.getForActor(businessProfileId, actor);
        if (!profileVersions.existsByBusinessProfileIdAndVersionNumber(profile.getId(), profile.getVersionNumber())) {
            throw new IllegalStateException("Current business profile version has no persisted snapshot");
        }
        List<RegulatoryRule> rules = ruleRepository.findByActiveTrue();
        List<RuleEngineService.AnalysisResult> results = ruleEngine.evaluate(profile, rules, requireVerifiedSources);
        ScrutinyAssessmentService.Assessment scrutiny = scrutinyAssessmentService.assess(profile);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("profileId", profile.getId());
        snapshot.put("profileVersion", profile.getVersionNumber());
        snapshot.put("ruleCount", rules.size());
        snapshot.put("verifiedSourcesRequired", requireVerifiedSources);
        snapshot.put("results", results);
        snapshot.put("scrutinyTier", scrutiny.tier());
        snapshot.put("scrutinyReasons", scrutiny.reasons());

        String ruleSetVersion = rules.stream()
                .map(r -> r.getCode() + ":v" + r.getVersionNumber())
                .sorted()
                .reduce((a, b) -> a + "," + b)
                .orElse("EMPTY");

        AnalysisRun run = new AnalysisRun(profile.getId(), profile.getVersionNumber(), ruleSetVersion, snapshot);
        analysisRepository.save(run);
        auditService.record(actor, "ANALYSIS_RUN_CREATED", "AnalysisRun", run.getId(),
                Map.of("businessProfileId", profile.getId(), "ruleSetVersion", ruleSetVersion));
        return AnalysisRunResponse.from(run, results);
    }

    @Transactional(readOnly = true)
    public AnalysisRunResponse latest(UUID businessProfileId, String actor) {
        profileService.getForActor(businessProfileId, actor);
        AnalysisRun run = analysisRepository.findFirstByBusinessProfileIdOrderByEvaluatedAtDesc(businessProfileId)
                .orElseThrow(() -> new IllegalArgumentException("No analysis run found for this business profile"));
        return getResponse(run);
    }

    @Transactional(readOnly = true)
    public AnalysisRunResponse get(UUID analysisRunId, String actor) {
        AnalysisRun run = analysisRepository.findById(analysisRunId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis run not found: " + analysisRunId));
        profileService.getForActor(run.getBusinessProfileId(), actor);
        return getResponse(run);
    }

    private AnalysisRunResponse getResponse(AnalysisRun run) {
        List<RuleEngineService.AnalysisResult> results = objectMapper.convertValue(
                run.getResultSnapshot().getOrDefault("results", List.of()),
                new TypeReference<>() {});
        return AnalysisRunResponse.from(run, results);
    }
}
