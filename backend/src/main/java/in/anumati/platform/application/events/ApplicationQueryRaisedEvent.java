package in.anumati.platform.application.events;

import java.util.UUID;

public record ApplicationQueryRaisedEvent(
        UUID applicationId, UUID businessProfileId, String recipientActor,
        String subject, String actor) {}
