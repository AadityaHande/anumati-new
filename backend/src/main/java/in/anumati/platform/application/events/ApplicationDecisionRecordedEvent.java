package in.anumati.platform.application.events;

import in.anumati.platform.application.DecisionOutcome;

import java.time.LocalDate;
import java.util.UUID;

public record ApplicationDecisionRecordedEvent(
        UUID applicationId, UUID businessProfileId, UUID approvalId,
        DecisionOutcome outcome, String recipientActor,
        LocalDate validFrom, LocalDate validUntil, UUID sourceId, String actor) {}
