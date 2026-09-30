package in.anumati.platform.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApplicationTimelineResponse(UUID applicationId, ApplicationStatus currentStatus,
                                          List<StatusEvent> statusHistory,
                                          List<ApplicationQueryResponseDto> queries,
                                          List<InspectionResponse> inspections,
                                          DecisionResponse decision) {
    public record StatusEvent(UUID id, ApplicationStatus fromStatus, ApplicationStatus toStatus, String actor,
                               String reason, Instant createdAt) {
        static StatusEvent from(ApplicationStatusHistory h) {
            return new StatusEvent(h.getId(), h.getFromStatus(), h.getToStatus(), h.getActor(), h.getReason(), h.getCreatedAt());
        }
    }
}
