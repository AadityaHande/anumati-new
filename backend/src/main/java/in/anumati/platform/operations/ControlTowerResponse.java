package in.anumati.platform.operations;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ControlTowerResponse(
        int total,
        int atRisk,
        int overdue,
        int blocked,
        List<Lane> lanes) {
    public record Lane(
            UUID applicationId,
            String reference,
            String approvalCode,
            String approvalName,
            String authority,
            String businessName,
            String district,
            String status,
            String slaStatus,
            Instant slaDueAt,
            List<String> blockers,
            String nextAction,
            Instant updatedAt) {}
}
