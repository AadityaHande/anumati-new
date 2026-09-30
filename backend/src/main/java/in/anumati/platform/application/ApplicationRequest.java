package in.anumati.platform.application;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ApplicationRequest(@NotNull UUID businessProfileId,
                                 @NotNull UUID analysisRunId,
                                 @NotNull UUID approvalId) {}
