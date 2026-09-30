package in.anumati.platform.regulatory;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "regulatory_rules")
public class RegulatoryRule {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 250)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false)
    private Approval approval;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private RegulatorySource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicabilityStatus outcome;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Long versionNumber;

    @Column(nullable = false)
    private boolean active;

    private LocalDate effectiveFrom;
    private LocalDate expiresOn;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<RuleCondition> conditions = new ArrayList<>();

    protected RegulatoryRule() {}

    public RegulatoryRule(String code, String name, Approval approval, RegulatorySource source,
                          ApplicabilityStatus outcome, Integer priority, Long versionNumber,
                          boolean active, LocalDate effectiveFrom, LocalDate expiresOn) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.approval = approval;
        this.source = source;
        this.outcome = outcome;
        this.priority = priority;
        this.versionNumber = versionNumber;
        this.active = active;
        this.effectiveFrom = effectiveFrom;
        this.expiresOn = expiresOn;
    }

    public void addCondition(RuleCondition condition) {
        condition.attachTo(this);
        this.conditions.add(condition);
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Approval getApproval() { return approval; }
    public RegulatorySource getSource() { return source; }
    public ApplicabilityStatus getOutcome() { return outcome; }
    public Integer getPriority() { return priority; }
    public Long getVersionNumber() { return versionNumber; }
    public boolean isActive() { return active; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getExpiresOn() { return expiresOn; }
    public List<RuleCondition> getConditions() { return conditions; }
}
