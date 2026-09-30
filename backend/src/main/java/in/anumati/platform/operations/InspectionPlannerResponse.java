package in.anumati.platform.operations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InspectionPlannerResponse(
        List<InspectionItem> inspections,
        List<CoordinationOpportunity> coordinationOpportunities) {
    public record InspectionItem(
            UUID inspectionId,
            UUID applicationId,
            String applicationReference,
            String businessName,
            String district,
            String authority,
            LocalDateTime scheduledAt,
            String assignedOfficer,
            String outcome) {}

    public record CoordinationOpportunity(
            UUID businessProfileId,
            String businessName,
            String district,
            LocalDate windowStart,
            LocalDate windowEnd,
            int inspectionCount,
            List<String> authorities) {}
}
