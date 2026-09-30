package in.anumati.platform.application.events;

import java.time.Instant;
import java.util.UUID;

public record ApplicationSubmittedEvent(
        UUID applicationId, UUID businessProfileId, UUID approvalId,
        String recipientActor, Instant submittedAt, String actor) {}
