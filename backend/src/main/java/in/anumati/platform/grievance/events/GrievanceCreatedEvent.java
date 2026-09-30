package in.anumati.platform.grievance.events;

import java.util.UUID;

public record GrievanceCreatedEvent(
        UUID grievanceId, UUID businessProfileId, String recipientActor, String actor) {}
