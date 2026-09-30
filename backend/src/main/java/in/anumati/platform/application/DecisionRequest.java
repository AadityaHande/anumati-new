package in.anumati.platform.application;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record DecisionRequest(
        @NotNull DecisionOutcome outcome,
        @Size(max = 4000) String decisionNotes,
        @Size(max = 120) String citedRuleCode,
        UUID citedSourceId,
        LocalDate validFrom,
        LocalDate validUntil) {}
