package in.anumati.platform.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QueryRequest(
        @NotBlank @Size(max = 200) String subject,
        @NotBlank @Size(max = 5000) String queryText) {}
