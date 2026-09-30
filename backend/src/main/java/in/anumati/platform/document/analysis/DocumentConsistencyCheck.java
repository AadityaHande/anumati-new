package in.anumati.platform.document.analysis;

import in.anumati.platform.document.Document;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_consistency_checks")
public class DocumentConsistencyCheck {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "field_name", nullable = false, length = 80)
    private String fieldName;

    @Column(name = "profile_value", length = 500)
    private String profileValue;

    @Column(name = "document_value", length = 500)
    private String documentValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsistencyStatus status;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private Instant checkedAt;

    protected DocumentConsistencyCheck() {}

    public DocumentConsistencyCheck(Document document, String fieldName, String profileValue,
                                    String documentValue, ConsistencyStatus status, String reason) {
        this.id = UUID.randomUUID();
        this.document = document;
        this.fieldName = fieldName;
        this.profileValue = profileValue;
        this.documentValue = documentValue;
        this.status = status;
        this.reason = reason;
        this.checkedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Document getDocument() { return document; }
    public String getFieldName() { return fieldName; }
    public String getProfileValue() { return profileValue; }
    public String getDocumentValue() { return documentValue; }
    public ConsistencyStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public Instant getCheckedAt() { return checkedAt; }
}
