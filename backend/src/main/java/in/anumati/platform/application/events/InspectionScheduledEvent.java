package in.anumati.platform.application.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record InspectionScheduledEvent(
        UUID applicationId, UUID inspectionId, UUID businessProfileId,
        String recipientActor, LocalDateTime scheduledAt, String actor) {}
