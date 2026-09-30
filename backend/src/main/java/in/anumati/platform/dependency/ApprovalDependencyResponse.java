package in.anumati.platform.dependency;

import java.util.UUID;

public record ApprovalDependencyResponse(
        UUID id,
        UUID approvalId,
        String approvalCode,
        String approvalName,
        UUID dependsOnApprovalId,
        String dependsOnApprovalCode,
        String dependsOnApprovalName,
        DependencyType dependencyType,
        String reason,
        boolean active) {

    public static ApprovalDependencyResponse from(ApprovalDependency d) {
        return new ApprovalDependencyResponse(
                d.getId(),
                d.getApproval().getId(),
                d.getApproval().getCode(),
                d.getApproval().getName(),
                d.getDependsOnApproval().getId(),
                d.getDependsOnApproval().getCode(),
                d.getDependsOnApproval().getName(),
                d.getDependencyType(),
                d.getReason(),
                d.isActive());
    }
}
