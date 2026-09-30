package in.anumati.platform.dependency;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ApprovalDependencyRequest(
        @NotNull UUID approvalId,
        @NotNull UUID dependsOnApprovalId,
        @NotNull DependencyType dependencyType,
        @Size(max = 500) String reason,
        boolean active) {}
