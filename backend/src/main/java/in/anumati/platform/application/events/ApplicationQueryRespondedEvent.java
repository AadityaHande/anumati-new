package in.anumati.platform.application.events;

import java.util.UUID;

public record ApplicationQueryRespondedEvent(
        UUID applicationId, UUID queryId, UUID businessProfileId,
        String recipientActor, String actor) {}
