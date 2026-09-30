package in.anumati.platform.application;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_queries")
public class ApplicationQuery {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationRecord application;
    @Column(nullable = false, length = 160)
    private String subject;
    @Column(name = "query_text", nullable = false, length = 5000)
    private String queryText;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QueryStatus status;
    @Column(name = "raised_by", nullable = false, length = 100)
    private String raisedBy;
    @Column(nullable = false)
    private Instant raisedAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected ApplicationQuery() {}
    public ApplicationQuery(ApplicationRecord application, String subject, String queryText, String raisedBy) {
        this.id = UUID.randomUUID();
        this.application = application;
        this.subject = subject;
        this.queryText = queryText;
        this.status = QueryStatus.OPEN;
        this.raisedBy = raisedBy;
        this.raisedAt = Instant.now();
        this.updatedAt = this.raisedAt;
    }
    public void markResponded() { this.status = QueryStatus.RESPONDED; this.updatedAt = Instant.now(); }
    public void close() { this.status = QueryStatus.CLOSED; this.updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public ApplicationRecord getApplication() { return application; }
    public String getSubject() { return subject; }
    public String getQueryText() { return queryText; }
    public QueryStatus getStatus() { return status; }
    public String getRaisedBy() { return raisedBy; }
    public Instant getRaisedAt() { return raisedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
