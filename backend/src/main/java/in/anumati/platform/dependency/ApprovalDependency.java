package in.anumati.platform.dependency;

import in.anumati.platform.regulatory.Approval;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_dependencies", uniqueConstraints = {
        @UniqueConstraint(name = "uq_approval_dependency", columnNames = {"approval_id", "depends_on_approval_id"})
})
public class ApprovalDependency {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_id", nullable = false)
    private Approval approval;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depends_on_approval_id", nullable = false)
    private Approval dependsOnApproval;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false, length = 30)
    private DependencyType dependencyType;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    protected ApprovalDependency() {}

    public ApprovalDependency(Approval approval, Approval dependsOnApproval, DependencyType dependencyType,
                              String reason, boolean active) {
        if (approval.getId().equals(dependsOnApproval.getId())) {
            throw new IllegalArgumentException("An approval cannot depend on itself");
        }
        this.id = UUID.randomUUID();
        this.approval = approval;
        this.dependsOnApproval = dependsOnApproval;
        this.dependencyType = dependencyType;
        this.reason = reason;
        this.active = active;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Approval getApproval() { return approval; }
    public Approval getDependsOnApproval() { return dependsOnApproval; }
    public DependencyType getDependencyType() { return dependencyType; }
    public String getReason() { return reason; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
