package in.anumati.platform.regulatory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "regulatory_sources")
public class RegulatorySource {
    @Id
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private SourceVerificationStatus verificationStatus;

    private LocalDate publishedOn;
    private LocalDate effectiveFrom;
    private LocalDate expiresOn;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "content_hash", length = 128)
    private String contentHash;

    protected RegulatorySource() {}

    public RegulatorySource(String title, String url, SourceType sourceType, SourceVerificationStatus verificationStatus,
                            LocalDate publishedOn, LocalDate effectiveFrom, LocalDate expiresOn,
                            Instant verifiedAt, String contentHash) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.url = url;
        this.sourceType = sourceType;
        this.verificationStatus = verificationStatus;
        this.publishedOn = publishedOn;
        this.effectiveFrom = effectiveFrom;
        this.expiresOn = expiresOn;
        this.verifiedAt = verifiedAt;
        this.contentHash = contentHash;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public SourceType getSourceType() { return sourceType; }
    public SourceVerificationStatus getVerificationStatus() { return verificationStatus; }
    public LocalDate getPublishedOn() { return publishedOn; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getExpiresOn() { return expiresOn; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public String getContentHash() { return contentHash; }
}
