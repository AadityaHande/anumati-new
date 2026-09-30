package in.anumati.platform.application;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inspections")
public class Inspection {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationRecord application;
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    @Column(name = "assigned_officer", nullable = false, length = 100)
    private String assignedOfficer;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InspectionOutcome outcome;
    @Column(name = "outcome_notes", length = 5000)
    private String outcomeNotes;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant completedAt;

    protected Inspection() {}
    public Inspection(ApplicationRecord application, LocalDateTime scheduledAt, String assignedOfficer) {
        this.id = UUID.randomUUID();
        this.application = application;
        this.scheduledAt = scheduledAt;
        this.assignedOfficer = assignedOfficer;
        this.outcome = InspectionOutcome.PENDING;
        this.createdAt = Instant.now();
    }
    public void complete(InspectionOutcome outcome, String notes) {
        if (outcome == InspectionOutcome.PENDING) throw new IllegalArgumentException("Inspection outcome cannot remain PENDING when completing an inspection");
        this.outcome = outcome;
        this.outcomeNotes = notes;
        this.completedAt = Instant.now();
    }
    public UUID getId() { return id; }
    public ApplicationRecord getApplication() { return application; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getAssignedOfficer() { return assignedOfficer; }
    public InspectionOutcome getOutcome() { return outcome; }
    public String getOutcomeNotes() { return outcomeNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
