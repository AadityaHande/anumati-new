package in.anumati.platform.business;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "business_profile_versions", uniqueConstraints = @UniqueConstraint(name = "uq_business_profile_version", columnNames = {"business_profile_id", "version_number"}))
public class BusinessProfileVersion {
    @Id
    private UUID id;
    @Column(name = "business_profile_id", nullable = false)
    private UUID businessProfileId;
    @Column(name = "version_number", nullable = false)
    private long versionNumber;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> snapshot;
    @Column(name = "change_type", nullable = false, length = 40)
    private String changeType;
    @Column(name = "captured_by", nullable = false, length = 200)
    private String capturedBy;
    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    protected BusinessProfileVersion() {}

    public BusinessProfileVersion(UUID businessProfileId, long versionNumber, Map<String, Object> snapshot, String changeType, String capturedBy) {
        this.id = UUID.randomUUID();
        this.businessProfileId = businessProfileId;
        this.versionNumber = versionNumber;
        this.snapshot = snapshot;
        this.changeType = changeType;
        this.capturedBy = capturedBy;
        this.capturedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getBusinessProfileId() { return businessProfileId; }
    public long getVersionNumber() { return versionNumber; }
    public Map<String, Object> getSnapshot() { return snapshot; }
    public String getChangeType() { return changeType; }
    public String getCapturedBy() { return capturedBy; }
    public Instant getCapturedAt() { return capturedAt; }
}
