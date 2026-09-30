package in.anumati.platform.regulatory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApprovalRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String authority,
        String purpose,
        @NotNull UUID sourceId,
        boolean active
) {}
