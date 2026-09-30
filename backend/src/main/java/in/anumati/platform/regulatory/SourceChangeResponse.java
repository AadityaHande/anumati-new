package in.anumati.platform.regulatory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SourceChangeResponse(
        String sourceFamily,
        boolean contentChanged,
        List<Version> versions) {
    public record Version(UUID id, String title, String url, String verificationStatus,
                          LocalDate publishedOn, LocalDate effectiveFrom, LocalDate expiresOn,
                          String contentHash) {}
}
