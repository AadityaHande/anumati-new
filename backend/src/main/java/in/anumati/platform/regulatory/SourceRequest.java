package in.anumati.platform.regulatory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SourceRequest(
        @NotBlank @Size(max = 250) String title,
        @NotBlank @Size(max = 1000) String url,
        @NotNull SourceType sourceType,
        @NotNull SourceVerificationStatus verificationStatus,
        LocalDate publishedOn,
        LocalDate effectiveFrom,
        LocalDate expiresOn,
        @Size(max = 128) @Pattern(regexp = "^$|[A-Fa-f0-9]{64}$", message = "contentHash must be a SHA-256 hex value") String contentHash
) {}
