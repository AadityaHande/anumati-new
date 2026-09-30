package in.anumati.platform.analysis;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "analysis_runs")
public class AnalysisRun {
    @Id
    private UUID id;

    @Column(name = "business_profile_id", nullable = false)
    private UUID businessProfileId;

    @Column(name = "profile_version", nullable = false)
    private Long profileVersion;

    @Column(name = "rule_set_version", nullable = false, length = 80)
    private String ruleSetVersion;

    @Column(nullable = false)
    private Instant evaluatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_snapshot", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> resultSnapshot;

    protected AnalysisRun() {}

    public AnalysisRun(UUID businessProfileId, Long profileVersion, String ruleSetVersion,
                       Map<String, Object> resultSnapshot) {
        this.id = UUID.randomUUID();
        this.businessProfileId = businessProfileId;
        this.profileVersion = profileVersion;
        this.ruleSetVersion = ruleSetVersion;
        this.evaluatedAt = Instant.now();
        this.resultSnapshot = resultSnapshot;
    }

    public UUID getId() { return id; }
    public UUID getBusinessProfileId() { return businessProfileId; }
    public Long getProfileVersion() { return profileVersion; }
    public String getRuleSetVersion() { return ruleSetVersion; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public Map<String, Object> getResultSnapshot() { return resultSnapshot; }
}
