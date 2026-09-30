package in.anumati.platform.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApplicationResponse(UUID id, UUID businessProfileId, UUID approvalId, String approvalCode,
                                  String approvalName, String authority, String externalReference,
                                  UUID analysisRunId, Long profileVersion, String matchedRuleCode, UUID sourceId,
                                  ApplicationStatus status, Instant submittedAt, Instant slaDueAt, Instant createdAt, Instant updatedAt,
                                  List<StatusEvent> statusHistory) {
    public static ApplicationResponse from(ApplicationRecord a, List<ApplicationStatusHistory> history) {
        return new ApplicationResponse(a.getId(), a.getBusinessProfile().getId(), a.getApproval().getId(),
                a.getApprovalCodeSnapshot(), a.getApprovalNameSnapshot(), a.getAuthoritySnapshot(),
                a.getExternalReference(), a.getAnalysisRunId(), a.getProfileVersion(), a.getMatchedRuleCode(),
                a.getSourceId(), a.getStatus(), a.getSubmittedAt(), a.getSlaDueAt(), a.getCreatedAt(), a.getUpdatedAt(),
                history.stream().map(StatusEvent::from).toList());
    }
    public record StatusEvent(UUID id, ApplicationStatus fromStatus, ApplicationStatus toStatus, String actor,
                              String reason, Instant createdAt) {
        static StatusEvent from(ApplicationStatusHistory h) {
            return new StatusEvent(h.getId(), h.getFromStatus(), h.getToStatus(), h.getActor(), h.getReason(), h.getCreatedAt());
        }
    }
}
