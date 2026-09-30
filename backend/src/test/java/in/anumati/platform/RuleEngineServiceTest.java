package in.anumati.platform;

import in.anumati.platform.analysis.RuleEngineService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessStage;
import in.anumati.platform.regulatory.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineServiceTest {

    private final RuleEngineService engine = new RuleEngineService();

    private BusinessProfile profile() {
        return new BusinessProfile(
                "Example Unit", "TEXTILE", "WEAVING", "PUNE", false,
                new BigDecimal("20000000"), "ABCDE1234F", "27ABCDE1234F1Z5", 50, java.util.Map.of(), new BigDecimal("250"), BusinessStage.SETUP, "test-user");
    }

    private RegulatorySource source(SourceVerificationStatus status) {
        return new RegulatorySource(
                "Test source", "https://example.gov.in/source", SourceType.RULE,
                status, null, LocalDate.now().minusDays(1), null,
                status == SourceVerificationStatus.VERIFIED ? Instant.now() : null, "hash");
    }

    @Test
    void evaluatesTypedConditionsDeterministically() {
        RegulatorySource source = source(SourceVerificationStatus.VERIFIED);
        Approval approval = new Approval("TEST-01", "Test approval", "Test Authority", "Test", source, true);
        RegulatoryRule rule = new RegulatoryRule("RULE-01", "Textile Pune rule", approval, source,
                ApplicabilityStatus.APPLICABLE, 100, 1L, true, LocalDate.now().minusDays(1), null);
        rule.addCondition(new RuleCondition(ConditionField.SECTOR, ConditionOperator.EQ, ValueType.STRING, "TEXTILE", 1));
        rule.addCondition(new RuleCondition(ConditionField.DISTRICT, ConditionOperator.EQ, ValueType.STRING, "PUNE", 2));
        rule.addCondition(new RuleCondition(ConditionField.EMPLOYEES, ConditionOperator.GTE, ValueType.NUMBER, "50", 3));

        List<RuleEngineService.AnalysisResult> results = engine.evaluate(profile(), List.of(rule), true);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().approvalId()).isEqualTo(approval.getId());
        assertThat(results.getFirst().status()).isEqualTo(ApplicabilityStatus.APPLICABLE);
        assertThat(results.getFirst().ruleCode()).isEqualTo("RULE-01");
    }

    @Test
    void rejectsExpiredRulesFromApplicability() {
        RegulatorySource source = source(SourceVerificationStatus.VERIFIED);
        Approval approval = new Approval("TEST-02", "Expired approval", "Authority", "Test", source, true);
        RegulatoryRule expired = new RegulatoryRule("RULE-02", "Expired", approval, source,
                ApplicabilityStatus.APPLICABLE, 100, 1L, true, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
        expired.addCondition(new RuleCondition(ConditionField.SECTOR, ConditionOperator.EQ, ValueType.STRING, "TEXTILE", 1));

        var results = engine.evaluate(profile(), List.of(expired), true);

        assertThat(results).isEmpty();
    }

    @Test
    void ignoresUnverifiedSourcesWhenRequired() {
        RegulatorySource source = source(SourceVerificationStatus.DRAFT);
        Approval approval = new Approval("TEST-03", "Unverified approval", "Authority", "Test", source, true);
        RegulatoryRule rule = new RegulatoryRule("RULE-03", "Unverified", approval, source,
                ApplicabilityStatus.APPLICABLE, 100, 1L, true, LocalDate.now().minusDays(1), null);
        rule.addCondition(new RuleCondition(ConditionField.SECTOR, ConditionOperator.EQ, ValueType.STRING, "TEXTILE", 1));

        var results = engine.evaluate(profile(), List.of(rule), true);

        assertThat(results).isEmpty();
    }

    @Test
    void higherPriorityMatchingRuleWins() {
        RegulatorySource source = source(SourceVerificationStatus.VERIFIED);
        Approval approval = new Approval("TEST-04", "Priority approval", "Authority", "Test", source, true);
        RegulatoryRule low = new RegulatoryRule("RULE-LOW", "Low", approval, source,
                ApplicabilityStatus.CONDITIONAL, 10, 1L, true, LocalDate.now().minusDays(1), null);
        low.addCondition(new RuleCondition(ConditionField.SECTOR, ConditionOperator.EQ, ValueType.STRING, "TEXTILE", 1));
        RegulatoryRule high = new RegulatoryRule("RULE-HIGH", "High", approval, source,
                ApplicabilityStatus.APPLICABLE, 100, 2L, true, LocalDate.now().minusDays(1), null);
        high.addCondition(new RuleCondition(ConditionField.SECTOR, ConditionOperator.EQ, ValueType.STRING, "TEXTILE", 1));

        var results = engine.evaluate(profile(), List.of(low, high), true);

        assertThat(results.getFirst().ruleCode()).isEqualTo("RULE-HIGH");
        assertThat(results.getFirst().status()).isEqualTo(ApplicabilityStatus.APPLICABLE);
    }
    @Test
    void evaluatesRegulatoryAttributeCondition() {
        RegulatorySource source = source(SourceVerificationStatus.VERIFIED);
        Approval approval = new Approval("TEST-ATTR", "Attribute approval", "Authority", "Test", source, true);
        RegulatoryRule rule = new RegulatoryRule("RULE-ATTR", "Environmental flag", approval, source,
                ApplicabilityStatus.APPLICABLE, 100, 1L, true, LocalDate.now().minusDays(1), null);
        rule.addCondition(new RuleCondition(ConditionField.REGULATORY_ATTRIBUTE, ConditionOperator.EQ,
                ValueType.STRING, "environmentalConsentRequired=true", 1));
        BusinessProfile flagged = new BusinessProfile("Example Unit", "MANUFACTURING", "PROCESSING", "PUNE", false,
                new BigDecimal("20000000"), null, null, 50, java.util.Map.of("environmentalConsentRequired", "true"),
                new BigDecimal("250"), BusinessStage.SETUP, "test-user");
        var results = engine.evaluate(flagged, List.of(rule), true);
        assertThat(results.getFirst().status()).isEqualTo(ApplicabilityStatus.APPLICABLE);
    }

    @Test
    void missingRegulatoryAttributeDoesNotMatch() {
        RegulatorySource source = source(SourceVerificationStatus.VERIFIED);
        Approval approval = new Approval("TEST-ATTR-MISSING", "Missing attribute approval", "Authority", "Test", source, true);
        RegulatoryRule rule = new RegulatoryRule("RULE-ATTR-MISSING", "Missing attr", approval, source, ApplicabilityStatus.APPLICABLE, 100, 1L, true, LocalDate.now().minusDays(1), null);
        rule.addCondition(new RuleCondition(ConditionField.REGULATORY_ATTRIBUTE, ConditionOperator.EQ, ValueType.STRING, "foodBusinessOperator=true", 1));
        var results = engine.evaluate(profile(), List.of(rule), true);
        assertThat(results.getFirst().status()).isEqualTo(ApplicabilityStatus.NOT_APPLICABLE);
    }

    @Test
    void regulatoryAttributeInOperatorMatches() {
        RegulatorySource source = source(SourceVerificationStatus.VERIFIED);
        Approval approval = new Approval("TEST-ATTR-IN", "Attribute set approval", "Authority", "Test", source, true);
        RegulatoryRule rule = new RegulatoryRule("RULE-ATTR-IN", "Attribute in", approval, source, ApplicabilityStatus.APPLICABLE, 100, 1L, true, LocalDate.now().minusDays(1), null);
        rule.addCondition(new RuleCondition(ConditionField.REGULATORY_ATTRIBUTE, ConditionOperator.IN, ValueType.STRING, "pollutionCategory=GREEN,ORANGE", 1));
        BusinessProfile flagged = new BusinessProfile("Example Unit", "MANUFACTURING", "PROCESSING", "PUNE", false, new BigDecimal("20000000"), null, null, 50, java.util.Map.of("pollutionCategory", "GREEN"), new BigDecimal("250"), BusinessStage.SETUP, "test-user");
        var results = engine.evaluate(flagged, List.of(rule), true);
        assertThat(results.getFirst().status()).isEqualTo(ApplicabilityStatus.APPLICABLE);
    }

}
