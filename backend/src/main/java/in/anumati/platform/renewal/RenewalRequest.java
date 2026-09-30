package in.anumati.platform.renewal;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record RenewalRequest(
        @NotNull UUID applicationId,
        LocalDate validFrom,
        @NotNull @FutureOrPresent LocalDate validUntil,
        @Min(0) int reminderDays,
        UUID sourceId) {}
