package in.anumati.platform.readiness;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DocumentRequirementRequest(
        @NotNull UUID approvalId,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 250) String documentName,
        @Size(max = 1000) String description,
        boolean mandatory,
        boolean active,
        @NotNull UUID sourceId) {}
