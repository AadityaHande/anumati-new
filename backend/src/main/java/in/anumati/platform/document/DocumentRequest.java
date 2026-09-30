package in.anumati.platform.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DocumentRequest(
        @NotNull UUID businessProfileId,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 255) String originalFilename,
        @NotBlank @Size(max = 120) String contentType,
        @NotNull @Positive Long sizeBytes) {}
