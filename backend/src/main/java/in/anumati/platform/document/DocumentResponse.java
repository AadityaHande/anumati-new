package in.anumati.platform.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, UUID businessProfileId, String category, String originalFilename,
                               String contentType, long declaredSizeBytes, Long storedSizeBytes,
                               DocumentStatus status, String eTag, Instant uploadedAt, Instant createdAt) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(d.getId(), d.getBusinessProfile().getId(), d.getCategory(), d.getOriginalFilename(),
                d.getContentType(), d.getDeclaredSizeBytes(), d.getStoredSizeBytes(), d.getStatus(),
                d.getETag(), d.getUploadedAt(), d.getCreatedAt());
    }
}
