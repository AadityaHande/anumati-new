package in.anumati.platform.compliance;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record ComplianceRequest(
        @NotNull UUID businessProfileId,
        UUID applicationId,
        UUID approvalId,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 120) String authority,
        @Size(max = 2000) String description,
        @NotNull @FutureOrPresent LocalDate dueDate,
        @Min(0) int reminderDays,
        @NotNull ComplianceFrequency frequency,
        @NotNull UUID sourceId) {}
