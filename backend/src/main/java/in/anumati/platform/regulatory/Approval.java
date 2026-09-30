package in.anumati.platform.regulatory;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "approvals")
public class Approval {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(nullable = false, length = 200)
    private String authority;

    @Column(length = 1000)
    private String purpose;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private RegulatorySource source;

    @Column(nullable = false)
    private boolean active;

    protected Approval() {}

    public Approval(String code, String name, String authority, String purpose, RegulatorySource source, boolean active) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.authority = authority;
        this.purpose = purpose;
        this.source = source;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAuthority() { return authority; }
    public String getPurpose() { return purpose; }
    public RegulatorySource getSource() { return source; }
    public boolean isActive() { return active; }
}
