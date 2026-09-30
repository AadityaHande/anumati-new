package in.anumati.platform.application;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "application_decisions")
public class Decision {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private ApplicationRecord application;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DecisionOutcome outcome;
    @Column(length = 5000)
    private String decisionNotes;
    @Column(name = "decided_by", nullable = false, length = 100)
    private String decidedBy;
    @Column(name = "cited_rule_code", length = 80)
    private String citedRuleCode;
    @Column(name = "cited_source_id")
    private UUID citedSourceId;
    @Column(nullable = false)
    private Instant decidedAt;
    @Column(name = "valid_from") private LocalDate validFrom;
    @Column(name = "valid_until") private LocalDate validUntil;

    protected Decision() {}
    public Decision(ApplicationRecord application, DecisionOutcome outcome, String decisionNotes, String decidedBy,
                     String citedRuleCode, UUID citedSourceId, LocalDate validFrom, LocalDate validUntil) {
        this.id = UUID.randomUUID();
        this.application = application;
        this.outcome = outcome;
        this.decisionNotes = decisionNotes;
        this.decidedBy = decidedBy;
        this.citedRuleCode = citedRuleCode;
        this.citedSourceId = citedSourceId;
        this.decidedAt = Instant.now();
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }
    public UUID getId() { return id; }
    public ApplicationRecord getApplication() { return application; }
    public DecisionOutcome getOutcome() { return outcome; }
    public String getDecisionNotes() { return decisionNotes; }
    public String getDecidedBy() { return decidedBy; }
    public String getCitedRuleCode() { return citedRuleCode; }
    public UUID getCitedSourceId() { return citedSourceId; }
    public Instant getDecidedAt() { return decidedAt; }
    public LocalDate getValidFrom(){return validFrom;}
    public LocalDate getValidUntil(){return validUntil;}
}
