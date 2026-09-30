package in.anumati.platform.grievance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GrievanceRequest(
        @NotNull UUID businessProfileId,
        UUID applicationId,
        @NotBlank @Size(max = 200) String subject,
        @NotBlank @Size(max = 5000) String description,
        @Size(max = 120) String department,
        @NotNull GrievancePriority priority) {}
