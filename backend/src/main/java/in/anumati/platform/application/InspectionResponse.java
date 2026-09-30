package in.anumati.platform.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record InspectionResponse(UUID id, UUID applicationId, LocalDateTime scheduledAt, String assignedOfficer,
                                 InspectionOutcome outcome, String outcomeNotes, Instant createdAt, Instant completedAt) {
    static InspectionResponse from(Inspection i) {
        return new InspectionResponse(i.getId(), i.getApplication().getId(), i.getScheduledAt(), i.getAssignedOfficer(),
                i.getOutcome(), i.getOutcomeNotes(), i.getCreatedAt(), i.getCompletedAt());
    }
}
