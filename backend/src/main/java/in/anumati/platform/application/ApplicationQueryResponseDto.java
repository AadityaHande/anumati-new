package in.anumati.platform.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApplicationQueryResponseDto(UUID id, UUID applicationId, String subject, String queryText,
                                          QueryStatus status, String raisedBy, Instant raisedAt, Instant updatedAt,
                                          List<ResponseItem> responses) {
    static ApplicationQueryResponseDto from(ApplicationQuery q, List<ApplicationQueryResponse> responses) {
        return new ApplicationQueryResponseDto(q.getId(), q.getApplication().getId(), q.getSubject(), q.getQueryText(),
                q.getStatus(), q.getRaisedBy(), q.getRaisedAt(), q.getUpdatedAt(),
                responses.stream().map(r -> new ResponseItem(r.getId(), r.getResponseText(), r.getResponder(), r.getAttachmentDocumentIds(), r.getCreatedAt())).toList());
    }
    public record ResponseItem(UUID id, String responseText, String responder, String attachmentDocumentIds, Instant createdAt) {}
}
