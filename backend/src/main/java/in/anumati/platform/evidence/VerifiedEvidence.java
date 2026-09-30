package in.anumati.platform.evidence;

import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.document.Document;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="verified_evidence", uniqueConstraints=@UniqueConstraint(name="uq_verified_evidence_profile_field_version", columnNames={"business_profile_id","field_name","profile_version"}))
public class VerifiedEvidence {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="business_profile_id", nullable=false) private BusinessProfile businessProfile;
    @Column(name="field_name", nullable=false, length=80) private String fieldName;
    @Column(name="field_value", nullable=false, length=500) private String fieldValue;
    @Column(name="profile_version", nullable=false) private Long profileVersion;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="source_document_id", nullable=false) private Document sourceDocument;
    @Column(name="verified_by", nullable=false, length=100) private String verifiedBy;
    @Column(name="verified_at", nullable=false) private Instant verifiedAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private VerifiedEvidenceStatus status;
    protected VerifiedEvidence() {}
    public VerifiedEvidence(BusinessProfile p,String field,String value,Document doc,String actor){this.id=UUID.randomUUID();this.businessProfile=p;this.fieldName=field;this.fieldValue=value;this.profileVersion=p.getVersionNumber();this.sourceDocument=doc;this.verifiedBy=actor;this.verifiedAt=Instant.now();this.status=VerifiedEvidenceStatus.VERIFIED;}
    public UUID getId(){return id;} public BusinessProfile getBusinessProfile(){return businessProfile;} public String getFieldName(){return fieldName;} public String getFieldValue(){return fieldValue;} public Long getProfileVersion(){return profileVersion;} public Document getSourceDocument(){return sourceDocument;} public String getVerifiedBy(){return verifiedBy;} public Instant getVerifiedAt(){return verifiedAt;} public VerifiedEvidenceStatus getStatus(){return status;}
}
