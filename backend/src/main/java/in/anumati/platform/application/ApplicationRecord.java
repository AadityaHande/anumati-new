package in.anumati.platform.application;

import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.regulatory.Approval;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "applications")
public class ApplicationRecord {
    @Version
    @Column(name = "entity_version", nullable = false)
    private Long entityVersion;
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false)
    private Approval approval;

    @Column(name = "analysis_run_id", nullable = false)
    private UUID analysisRunId;

    @Column(name = "profile_version", nullable = false)
    private Long profileVersion;

    @Column(name = "approval_code_snapshot", nullable = false, length = 80)
    private String approvalCodeSnapshot;

    @Column(name = "approval_name_snapshot", nullable = false, length = 250)
    private String approvalNameSnapshot;

    @Column(name = "authority_snapshot", nullable = false, length = 200)
    private String authoritySnapshot;

    @Column(name = "matched_rule_code", length = 80)
    private String matchedRuleCode;

    @Column(name = "source_id")
    private UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus status;

    @Column(name = "external_reference", unique = true, length = 100)
    private String externalReference;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ApplicationRecord() {}

    public ApplicationRecord(BusinessProfile businessProfile, Approval approval, UUID analysisRunId,
                             long profileVersion, String approvalCodeSnapshot, String approvalNameSnapshot,
                             String authoritySnapshot, String matchedRuleCode, UUID sourceId,
                             String externalReference) {
        this.id = UUID.randomUUID();
        this.businessProfile = businessProfile;
        this.approval = approval;
        this.analysisRunId = analysisRunId;
        this.profileVersion = profileVersion;
        this.approvalCodeSnapshot = approvalCodeSnapshot;
        this.approvalNameSnapshot = approvalNameSnapshot;
        this.authoritySnapshot = authoritySnapshot;
        this.matchedRuleCode = matchedRuleCode;
        this.sourceId = sourceId;
        this.status = ApplicationStatus.SUBMITTED;
        this.externalReference = externalReference;
        this.submittedAt = Instant.now();
        this.createdAt = this.submittedAt;
        this.updatedAt = this.submittedAt;
    }

    public void transitionTo(ApplicationStatus next) {
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public BusinessProfile getBusinessProfile() { return businessProfile; }
    public Approval getApproval() { return approval; }
    public UUID getAnalysisRunId() { return analysisRunId; }
    public Long getProfileVersion() { return profileVersion; }
    public String getApprovalCodeSnapshot() { return approvalCodeSnapshot; }
    public String getApprovalNameSnapshot() { return approvalNameSnapshot; }
    public String getAuthoritySnapshot() { return authoritySnapshot; }
    public String getMatchedRuleCode() { return matchedRuleCode; }
    public UUID getSourceId() { return sourceId; }
    public ApplicationStatus getStatus() { return status; }
    public String getExternalReference() { return externalReference; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getSlaDueAt() { return slaDueAt; }
    public void setSlaDueAt(Instant value) { this.slaDueAt = value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
