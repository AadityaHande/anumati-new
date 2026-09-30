package in.anumati.platform.document;

import java.time.Instant;
import java.util.UUID;

public record PresignResponse(UUID documentId, String objectKey, String uploadUrl, Instant expiresAt, DocumentStatus status) {}
