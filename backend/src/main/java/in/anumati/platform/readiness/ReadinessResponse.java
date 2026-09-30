package in.anumati.platform.readiness;

import java.util.List;
import java.util.UUID;

public record ReadinessResponse(
        UUID businessProfileId,
        UUID analysisRunId,
        int overallScore,
        int knownRequirements,
        int satisfiedRequirements,
        List<ApprovalReadiness> approvals) {

    public record ApprovalReadiness(
            UUID approvalId,
            String approvalCode,
            String approvalName,
            String status,
            int score,
            int mandatoryKnown,
            int mandatorySatisfied,
            List<RequirementStatus> requirements) {}

    public record RequirementStatus(
            UUID requirementId,
            String category,
            String documentName,
            boolean mandatory,
            boolean satisfied,
            String status,
            List<UUID> matchingDocumentIds) {}
}
