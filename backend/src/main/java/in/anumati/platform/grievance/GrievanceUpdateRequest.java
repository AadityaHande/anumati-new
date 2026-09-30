package in.anumati.platform.grievance;

import jakarta.validation.constraints.Size;

public record GrievanceUpdateRequest(
        GrievanceStatus status,
        @Size(max = 120) String assignedTo,
        @Size(max = 5000) String resolution) {}
