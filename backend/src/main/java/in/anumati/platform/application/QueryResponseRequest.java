package in.anumati.platform.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record QueryResponseRequest(
        @NotBlank @Size(max = 5000) String responseText,
        List<UUID> attachmentDocumentIds) {}
