package in.anumati.platform.application;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record InspectionRequest(
        @NotNull @Future LocalDateTime scheduledAt,
        @NotBlank @Size(max = 120) String assignedOfficer) {}
