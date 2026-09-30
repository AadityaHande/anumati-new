package in.anumati.platform.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DecisionResponse(UUID id, UUID applicationId, DecisionOutcome outcome, String decisionNotes,
                               String decidedBy, String citedRuleCode, UUID citedSourceId, Instant decidedAt, LocalDate validFrom, LocalDate validUntil) {
    static DecisionResponse from(Decision d) {
        return new DecisionResponse(d.getId(), d.getApplication().getId(), d.getOutcome(), d.getDecisionNotes(),
                d.getDecidedBy(), d.getCitedRuleCode(), d.getCitedSourceId(), d.getDecidedAt(), d.getValidFrom(), d.getValidUntil());
    }
}
