package in.anumati.platform.sla;

import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.regulatory.RegulatorySource;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "approval_sla_configs")
public class ApprovalSlaConfig {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false, unique = true)
    private Approval approval;
    @Column(name = "target_hours", nullable = false) private Integer targetHours;
    @Column(name = "warning_hours", nullable = false) private Integer warningHours;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private RegulatorySource source;
    @Column(nullable = false) private boolean active;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scope_attributes", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> scopeAttributes = new LinkedHashMap<>();
    @Column(name = "max_investment_inr", precision = 18, scale = 2)
    private BigDecimal maxInvestmentInr;
    @Column(nullable = false) private Instant updatedAt;

    protected ApprovalSlaConfig() {}

    public ApprovalSlaConfig(Approval approval, int targetHours, int warningHours, RegulatorySource source,
                             boolean active, Map<String, String> scopeAttributes, BigDecimal maxInvestmentInr) {
        this.id = UUID.randomUUID();
        this.approval = approval;
        this.targetHours = targetHours;
        this.warningHours = warningHours;
        this.source = source;
        this.active = active;
        this.scopeAttributes = normalize(scopeAttributes);
        this.maxInvestmentInr = maxInvestmentInr;
        this.updatedAt = Instant.now();
    }

    public ApprovalSlaConfig(Approval approval, int targetHours, int warningHours, RegulatorySource source, boolean active) {
        this(approval, targetHours, warningHours, source, active, Map.of(), null);
    }

    public ApprovalSlaConfig(Approval approval, int targetHours, int warningHours, RegulatorySource source, boolean active, Map<String, String> scopeAttributes) {
        this(approval, targetHours, warningHours, source, active, scopeAttributes, null);
    }

    public void update(int targetHours, int warningHours, RegulatorySource source, boolean active,
                       Map<String, String> scopeAttributes, BigDecimal maxInvestmentInr) {
        this.targetHours = targetHours;
        this.warningHours = warningHours;
        this.source = source;
        this.active = active;
        this.scopeAttributes = normalize(scopeAttributes);
        this.maxInvestmentInr = maxInvestmentInr;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Approval getApproval() { return approval; }
    public int getTargetHours() { return targetHours; }
    public int getWarningHours() { return warningHours; }
    public RegulatorySource getSource() { return source; }
    public boolean isActive() { return active; }
    public Map<String, String> getScopeAttributes() { return Map.copyOf(scopeAttributes); }
    public BigDecimal getMaxInvestmentInr() { return maxInvestmentInr; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static Map<String, String> normalize(Map<String, String> scope) {
        if (scope == null || scope.isEmpty()) return new LinkedHashMap<>();
        Map<String, String> out = new LinkedHashMap<>();
        scope.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
                out.put(key.trim(), value.trim());
            }
        });
        return out;
    }
}
