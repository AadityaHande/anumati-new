package in.anumati.platform.regulatory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RegulatorySourceResponse(UUID id, String title, String url, SourceType sourceType,
                                       SourceVerificationStatus verificationStatus, LocalDate publishedOn,
                                       LocalDate effectiveFrom, LocalDate expiresOn, Instant verifiedAt,
                                       String contentHash) {
    static RegulatorySourceResponse from(RegulatorySource s) {
        return new RegulatorySourceResponse(s.getId(), s.getTitle(), s.getUrl(), s.getSourceType(),
                s.getVerificationStatus(), s.getPublishedOn(), s.getEffectiveFrom(), s.getExpiresOn(),
                s.getVerifiedAt(), s.getContentHash());
    }
}
