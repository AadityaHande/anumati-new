package in.anumati.platform.readiness;

import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.regulatory.RegulatorySource;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_document_requirements", uniqueConstraints = {
        @UniqueConstraint(name = "uq_approval_document_requirement", columnNames = {"approval_id", "category"})
})
public class DocumentRequirement {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false)
    private Approval approval;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "document_name", nullable = false, length = 250)
    private String documentName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean mandatory;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private RegulatorySource source;

    @Column(nullable = false)
    private Instant createdAt;

    protected DocumentRequirement() {}

    public DocumentRequirement(Approval approval, String category, String documentName, String description,
                               boolean mandatory, boolean active, RegulatorySource source) {
        this.id = UUID.randomUUID();
        this.approval = approval;
        this.category = normalize(category);
        this.documentName = documentName;
        this.description = description;
        this.mandatory = mandatory;
        this.active = active;
        this.source = source;
        this.createdAt = Instant.now();
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase(java.util.Locale.ROOT).replaceAll("\\s+", "_");
    }

    public UUID getId() { return id; }
    public Approval getApproval() { return approval; }
    public String getCategory() { return category; }
    public String getDocumentName() { return documentName; }
    public String getDescription() { return description; }
    public boolean isMandatory() { return mandatory; }
    public boolean isActive() { return active; }
    public RegulatorySource getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }
}
