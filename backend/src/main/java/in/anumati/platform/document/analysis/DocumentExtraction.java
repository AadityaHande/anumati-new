package in.anumati.platform.document.analysis;

import in.anumati.platform.document.Document;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_extractions")
public class DocumentExtraction {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(nullable = false, length = 40)
    private String engine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentAnalysisStatus status;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "extracted_fields_json", nullable = false, columnDefinition = "jsonb")
    private String extractedFieldsJson;

    @Column(nullable = false)
    private Instant analyzedAt;

    @Column(length = 1000)
    private String errorMessage;

    protected DocumentExtraction() {}

    public DocumentExtraction(Document document, String engine, DocumentAnalysisStatus status,
                              String extractedText, String extractedFieldsJson, String errorMessage) {
        this.id = UUID.randomUUID();
        this.document = document;
        update(engine, status, extractedText, extractedFieldsJson, errorMessage);
    }

    public void update(String engine, DocumentAnalysisStatus status, String extractedText,
                       String extractedFieldsJson, String errorMessage) {
        this.engine = engine;
        this.status = status;
        this.extractedText = extractedText;
        this.extractedFieldsJson = extractedFieldsJson;
        this.analyzedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public UUID getId() { return id; }
    public Document getDocument() { return document; }
    public String getEngine() { return engine; }
    public DocumentAnalysisStatus getStatus() { return status; }
    public String getExtractedText() { return extractedText; }
    public String getExtractedFieldsJson() { return extractedFieldsJson; }
    public Instant getAnalyzedAt() { return analyzedAt; }
    public String getErrorMessage() { return errorMessage; }
}
