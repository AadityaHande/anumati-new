package in.anumati.platform.application;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_query_responses")
public class ApplicationQueryResponse {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "query_id", nullable = false)
    private ApplicationQuery query;
    @Column(name = "response_text", nullable = false, length = 5000)
    private String responseText;
    @Column(nullable = false, length = 100)
    private String responder;
    @Column(name = "attachment_document_ids", columnDefinition = "jsonb")
    private String attachmentDocumentIds;
    @Column(nullable = false)
    private Instant createdAt;

    protected ApplicationQueryResponse() {}
    public ApplicationQueryResponse(ApplicationQuery query, String responseText, String responder, String attachmentDocumentIds) {
        this.id = UUID.randomUUID();
        this.query = query;
        this.responseText = responseText;
        this.responder = responder;
        this.attachmentDocumentIds = attachmentDocumentIds;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public ApplicationQuery getQuery() { return query; }
    public String getResponseText() { return responseText; }
    public String getResponder() { return responder; }
    public String getAttachmentDocumentIds() { return attachmentDocumentIds; }
    public Instant getCreatedAt() { return createdAt; }
}
