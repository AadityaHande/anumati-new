package in.anumati.platform.application;

import jakarta.validation.constraints.NotNull;

public record StatusTransitionRequest(@NotNull ApplicationStatus targetStatus, String reason) {}
