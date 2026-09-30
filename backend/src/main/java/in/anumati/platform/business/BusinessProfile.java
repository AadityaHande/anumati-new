package in.anumati.platform.business;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;

@Entity
@Table(name = "business_profiles")
public class BusinessProfile {
    @Id
    private UUID id;

    @NotBlank
    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String sector;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String activity;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String district;

    @NotNull
    @Column(name = "midc_unit", nullable = false)
    private Boolean midcUnit;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 18, scale = 2)
    private BigDecimal investmentInr;

    @Pattern(regexp = "^$|[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN format")
    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Pattern(regexp = "^$|[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$", message = "Invalid GSTIN format")
    @Column(name = "gstin", length = 15)
    private String gstin;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer employees;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "regulatory_attributes", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> regulatoryAttributes = new LinkedHashMap<>();

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "power_usage_kw", precision = 12, scale = 2)
    private BigDecimal powerUsageKw;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "business_stage", nullable = false, length = 30)
    private BusinessStage businessStage;

    @Column(nullable = false)
    private Long versionNumber;

    @Version
    @Column(name = "entity_version", nullable = false)
    private Long entityVersion;

    @Column(nullable = false, length = 100)
    private String ownerActor;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected BusinessProfile() {}

    public BusinessProfile(String businessName, String sector, String activity, String district,
                           Boolean midcUnit, BigDecimal investmentInr, String panNumber, String gstin, Integer employees,
                           Map<String, String> regulatoryAttributes, BigDecimal powerUsageKw, BusinessStage businessStage, String ownerActor) {
        this.id = UUID.randomUUID();
        this.businessName = businessName;
        this.sector = sector;
        this.activity = activity;
        this.district = district;
        this.midcUnit = midcUnit;
        this.investmentInr = investmentInr;
        this.panNumber = blankToNull(panNumber);
        this.gstin = blankToNull(gstin);
        this.employees = employees;
        this.regulatoryAttributes = normalizeAttributes(regulatoryAttributes);
        this.powerUsageKw = powerUsageKw;
        this.businessStage = businessStage;
        this.versionNumber = 1L;
        this.ownerActor = ownerActor;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public String getBusinessName() { return businessName; }
    public String getSector() { return sector; }
    public String getActivity() { return activity; }
    public String getDistrict() { return district; }
    public Boolean getMidcUnit() { return midcUnit; }
    public BigDecimal getInvestmentInr() { return investmentInr; }
    public String getPanNumber() { return panNumber; }
    public String getGstin() { return gstin; }
    public Integer getEmployees() { return employees; }
    public Map<String, String> getRegulatoryAttributes() { return Map.copyOf(regulatoryAttributes); }
    public BigDecimal getPowerUsageKw() { return powerUsageKw; }
    public BusinessStage getBusinessStage() { return businessStage; }
    public Long getVersionNumber() { return versionNumber; }
    public String getOwnerActor() { return ownerActor; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String businessName, String sector, String activity, String district,
                       Boolean midcUnit, BigDecimal investmentInr, String panNumber, String gstin,
                       Integer employees, Map<String, String> regulatoryAttributes, BigDecimal powerUsageKw, BusinessStage businessStage) {
        this.businessName = businessName;
        this.sector = sector;
        this.activity = activity;
        this.district = district;
        this.midcUnit = midcUnit;
        this.investmentInr = investmentInr;
        this.panNumber = blankToNull(panNumber);
        this.gstin = blankToNull(gstin);
        this.employees = employees;
        this.regulatoryAttributes = normalizeAttributes(regulatoryAttributes);
        this.powerUsageKw = powerUsageKw;
        this.businessStage = businessStage;
        this.versionNumber++;
        this.updatedAt = Instant.now();
    }

    private static Map<String, String> normalizeAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) return new LinkedHashMap<>();
        Map<String, String> normalized = new LinkedHashMap<>();
        attributes.forEach((k, v) -> {
            if (k != null && !k.isBlank() && v != null) normalized.put(k.trim(), v.trim());
        });
        return normalized;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
