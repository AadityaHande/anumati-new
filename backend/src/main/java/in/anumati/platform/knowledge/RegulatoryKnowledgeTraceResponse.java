package in.anumati.platform.knowledge;

import java.util.List;
import java.util.UUID;

public record RegulatoryKnowledgeTraceResponse(
        UUID approvalId, String approvalCode, String approvalName, String authority,
        List<RuleTrace> rules, List<DocumentTrace> documents, List<DependencyTrace> dependencies) {
    public record RuleTrace(UUID ruleId, String code, String name, String outcome, long version, UUID sourceId, String sourceTitle, String sourceUrl) {}
    public record DocumentTrace(UUID documentRequirementId, String name, boolean mandatory, UUID sourceId, String sourceTitle, String sourceUrl) {}
    public record DependencyTrace(UUID approvalId, String approvalCode, String approvalName, String type) {}
}
