package in.anumati.platform.document.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DocumentAnalysisResponse(
        UUID documentId,
        DocumentAnalysisStatus extractionStatus,
        String engine,
        Map<String, String> extractedFields,
        List<Consistency> consistency,
        String overallStatus,
        Instant analyzedAt,
        String errorMessage) {
    public record Consistency(String fieldName, String profileValue, String documentValue,
                              ConsistencyStatus status, String reason) {}
}
