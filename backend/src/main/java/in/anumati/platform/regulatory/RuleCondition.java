package in.anumati.platform.regulatory;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "rule_conditions")
public class RuleCondition {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private RegulatoryRule rule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConditionField field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionOperator operator;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private ValueType valueType;

    @Column(nullable = false, length = 500)
    private String value;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    protected RuleCondition() {}

    public RuleCondition(ConditionField field, ConditionOperator operator, ValueType valueType, String value, Integer sequenceNumber) {
        this.id = UUID.randomUUID();
        this.field = field;
        this.operator = operator;
        this.valueType = valueType;
        this.value = value;
        this.sequenceNumber = sequenceNumber;
    }

    void attachTo(RegulatoryRule rule) { this.rule = rule; }

    public ConditionField getField() { return field; }
    public ConditionOperator getOperator() { return operator; }
    public ValueType getValueType() { return valueType; }
    public String getValue() { return value; }
}
