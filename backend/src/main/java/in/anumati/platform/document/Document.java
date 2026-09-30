package in.anumati.platform.document;

import in.anumati.platform.business.BusinessProfile;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_documents")
public class Document {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;
    @Column(nullable = false, length = 80) private String category;
    @Column(name = "normalized_category", nullable = false, length = 80) private String normalizedCategory;
    @Column(nullable = false, length = 255) private String originalFilename;
    @Column(nullable = false, length = 120) private String contentType;
    @Column(nullable = false) private Long declaredSizeBytes;
    @Column(nullable = false, unique = true, length = 800) private String objectKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private DocumentStatus status;
    private Long storedSizeBytes;
    @Column(length = 200) private String eTag;
    private Instant uploadedAt;
    @Column(nullable = false) private Instant createdAt;
    @Column(name = "validated_profile_version") private Long validatedProfileVersion;

    protected Document() {}

    public Document(BusinessProfile businessProfile, String category, String originalFilename,
                    String contentType, long declaredSizeBytes, String objectKey) {
        this.id = UUID.randomUUID();
        this.businessProfile = businessProfile;
        this.category = category;
        this.normalizedCategory = normalizeCategory(category);
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.declaredSizeBytes = declaredSizeBytes;
        this.objectKey = objectKey;
        this.status = DocumentStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void markUploaded(long storedSizeBytes, String eTag) {
        this.storedSizeBytes = storedSizeBytes;
        this.eTag = eTag;
        this.uploadedAt = Instant.now();
        this.status = DocumentStatus.UPLOADED;
        this.validatedProfileVersion = null;
    }

    public void markNeedsReview() { this.status = DocumentStatus.NEEDS_REVIEW; this.validatedProfileVersion = null; }
    public void markReady(long profileVersion) { this.status = DocumentStatus.READY; this.validatedProfileVersion = profileVersion; }
    public UUID getId(){return id;}
    public BusinessProfile getBusinessProfile(){return businessProfile;}
    public String getCategory(){return category;}
    public String getNormalizedCategory(){return normalizedCategory;}

    private static String normalizeCategory(String value) {
        return value.trim().toUpperCase(java.util.Locale.ROOT).replaceAll("\\s+", "_");
    }
    public String getOriginalFilename(){return originalFilename;}
    public String getContentType(){return contentType;}
    public Long getDeclaredSizeBytes(){return declaredSizeBytes;}
    public String getObjectKey(){return objectKey;}
    public DocumentStatus getStatus(){return status;}
    public Long getStoredSizeBytes(){return storedSizeBytes;}
    public String getETag(){return eTag;}
    public Instant getUploadedAt(){return uploadedAt;}
    public Instant getCreatedAt(){return createdAt;}
    public Long getValidatedProfileVersion(){return validatedProfileVersion;}
}
