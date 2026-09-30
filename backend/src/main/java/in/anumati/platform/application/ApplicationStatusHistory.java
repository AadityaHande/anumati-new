package in.anumati.platform.application;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistory {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationRecord application;
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 40)
    private ApplicationStatus fromStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 40)
    private ApplicationStatus toStatus;
    @Column(nullable = false, length = 100)
    private String actor;
    @Column(length = 1000)
    private String reason;
    @Column(nullable = false)
    private Instant createdAt;

    protected ApplicationStatusHistory() {}

    public ApplicationStatusHistory(ApplicationRecord application, ApplicationStatus fromStatus,
                                     ApplicationStatus toStatus, String actor, String reason) {
        this.id = UUID.randomUUID();
        this.application = application;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actor = actor;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public ApplicationRecord getApplication() { return application; }
    public ApplicationStatus getFromStatus() { return fromStatus; }
    public ApplicationStatus getToStatus() { return toStatus; }
    public String getActor() { return actor; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
