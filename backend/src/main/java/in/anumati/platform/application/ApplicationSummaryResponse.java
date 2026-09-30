package in.anumati.platform.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationSummaryResponse(UUID id, String externalReference, UUID approvalId, String approvalCode,
                                         String approvalName, ApplicationStatus status, UUID analysisRunId,
                                         Long profileVersion, Instant submittedAt, Instant updatedAt, Instant slaDueAt) {
    public static ApplicationSummaryResponse from(ApplicationRecord a) {
        return new ApplicationSummaryResponse(a.getId(), a.getExternalReference(), a.getApproval().getId(),
                a.getApprovalCodeSnapshot(), a.getApprovalNameSnapshot(), a.getStatus(), a.getAnalysisRunId(),
                a.getProfileVersion(), a.getSubmittedAt(), a.getUpdatedAt(), a.getSlaDueAt());
    }
}
