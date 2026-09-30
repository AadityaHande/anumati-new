package in.anumati.platform.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InspectionCompletionRequest(
        @NotNull InspectionOutcome outcome,
        @Size(max = 2000) String outcomeNotes) {}
