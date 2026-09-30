package in.anumati.platform.preflight;

import java.util.List;
import java.util.UUID;

public record PreflightResponse(
        UUID businessProfileId, UUID analysisRunId, long profileVersion,
        String status, int blockerCount, int warningCount,
        List<Check> checks, List<ApprovalCheck> approvals) {
    public record Check(String code, String severity, String title, String detail) {}
    public record ApprovalCheck(UUID approvalId, String approvalCode, String approvalName, String readinessStatus, boolean hasBlockingDependencies, List<String> blockers) {}
}
