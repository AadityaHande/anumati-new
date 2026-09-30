package in.anumati.platform.analysis;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisRunResponse(
        UUID id,
        UUID businessProfileId,
        long profileVersion,
        String ruleSetVersion,
        Instant evaluatedAt,
        String scrutinyTier,
        List<String> scrutinyReasons,
        List<RuleEngineService.AnalysisResult> results) {

    public static AnalysisRunResponse from(AnalysisRun run, List<RuleEngineService.AnalysisResult> results) {
        String tier = String.valueOf(run.getResultSnapshot().getOrDefault("scrutinyTier", "STANDARD"));
        Object rawReasons = run.getResultSnapshot().getOrDefault("scrutinyReasons", List.of());
        List<String> reasons = rawReasons instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : List.of();
        return new AnalysisRunResponse(run.getId(), run.getBusinessProfileId(), run.getProfileVersion(),
                run.getRuleSetVersion(), run.getEvaluatedAt(), tier, reasons, results);
    }
}
