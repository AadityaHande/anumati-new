package in.anumati.platform.regulatory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RuleRequest(
        @NotBlank @Size(max = 80) String code,
        @NotBlank @Size(max = 250) String name,
        @NotNull UUID approvalId,
        @NotNull UUID sourceId,
        @NotNull ApplicabilityStatus outcome,
        @NotNull @Positive Integer priority,
        @NotNull @Positive Long versionNumber,
        boolean active,
        LocalDate effectiveFrom,
        LocalDate expiresOn,
        @NotNull List<@Valid RuleConditionRequest> conditions
) {}
