package in.anumati.platform.analysis;

import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.regulatory.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleEngineService {

    public List<AnalysisResult> evaluate(BusinessProfile profile, List<RegulatoryRule> rules, boolean requireVerifiedSources) {
        LocalDate today = LocalDate.now();
        List<RegulatoryRule> eligibleRules = rules.stream()
                .filter(RegulatoryRule::isActive)
                .filter(r -> !requireVerifiedSources || r.getSource().getVerificationStatus() == SourceVerificationStatus.VERIFIED)
                .filter(r -> r.getEffectiveFrom() == null || !today.isBefore(r.getEffectiveFrom()))
                .filter(r -> r.getExpiresOn() == null || !today.isAfter(r.getExpiresOn()))
                .toList();

        Map<UUID, List<RegulatoryRule>> byApproval = eligibleRules.stream()
                .collect(Collectors.groupingBy(r -> r.getApproval().getId()));

        List<AnalysisResult> results = new ArrayList<>();
        for (List<RegulatoryRule> approvalRules : byApproval.values()) {
            approvalRules.sort(Comparator.comparing(RegulatoryRule::getPriority).reversed());
            Optional<RegulatoryRule> matched = approvalRules.stream().filter(r -> matches(profile, r)).findFirst();
            RegulatoryRule representative = approvalRules.getFirst();

            if (matched.isPresent()) {
                RegulatoryRule rule = matched.get();
                results.add(new AnalysisResult(
                        rule.getApproval().getId(), rule.getApproval().getCode(), rule.getApproval().getName(), rule.getApproval().getAuthority(),
                        rule.getOutcome(), rule.getCode(), rule.getSource().getId(), rule.getSource().getTitle(),
                        rule.getSource().getUrl(), buildReason(rule),
                        "MATCHED_RULE"));
            } else {
                results.add(new AnalysisResult(
                        representative.getApproval().getId(), representative.getApproval().getCode(), representative.getApproval().getName(), representative.getApproval().getAuthority(),
                        ApplicabilityStatus.NOT_APPLICABLE, null, representative.getSource().getId(), representative.getSource().getTitle(),
                        representative.getSource().getUrl(),
                        "No active rule matched this profile within the current verified rule set.",
                        "NO_MATCHING_RULE_IN_CURRENT_KNOWLEDGE_BASE"));
            }
        }
        return results;
    }

    private boolean matches(BusinessProfile profile, RegulatoryRule rule) {
        return rule.getConditions().stream().allMatch(c -> {
            if (c.getField() == ConditionField.REGULATORY_ATTRIBUTE) {
                String[] parts = c.getValue().split("=", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException("REGULATORY_ATTRIBUTE condition must use key=value syntax");
                }
                String actual = profile.getRegulatoryAttributes().get(parts[0].trim());
                if (actual == null) return false;
                String expected = parts[1].trim();
                return switch (c.getOperator()) {
                    case EQ -> actual.equalsIgnoreCase(expected);
                    case NEQ -> !actual.equalsIgnoreCase(expected);
                    case IN -> Arrays.stream(expected.split(",")).map(String::trim)
                            .anyMatch(value -> actual.equalsIgnoreCase(value));
                    case NOT_IN -> Arrays.stream(expected.split(",")).map(String::trim)
                            .noneMatch(value -> actual.equalsIgnoreCase(value));
                    default -> throw new IllegalArgumentException(
                            "Unsupported REGULATORY_ATTRIBUTE operator: " + c.getOperator());
                };
            }
            return compare(valueFor(profile, c.getField()), c);
        });
    }

    private Object valueFor(BusinessProfile p, ConditionField field) {
        return switch (field) {
            case SECTOR -> p.getSector();
            case ACTIVITY -> p.getActivity();
            case DISTRICT -> p.getDistrict();
            case MIDC_UNIT -> p.getMidcUnit();
            case INVESTMENT_INR -> p.getInvestmentInr();
            case EMPLOYEES -> p.getEmployees();
            case POWER_USAGE_KW -> p.getPowerUsageKw();
            case BUSINESS_STAGE -> p.getBusinessStage().name();
            case REGULATORY_ATTRIBUTE -> null;
        };
    }

    private boolean compare(Object actual, RuleCondition condition) {
        if (condition.getValueType() == ValueType.STRING) {
            return compareStrings(String.valueOf(actual), condition);
        }
        if (condition.getValueType() == ValueType.BOOLEAN) {
            boolean expected = Boolean.parseBoolean(condition.getValue());
            boolean current = (Boolean) actual;
            return switch (condition.getOperator()) {
                case EQ -> current == expected;
                case NEQ -> current != expected;
                default -> throw new IllegalArgumentException("Unsupported boolean operator: " + condition.getOperator());
            };
        }
        if (condition.getValueType() == ValueType.NUMBER) {
            BigDecimal current = new BigDecimal(String.valueOf(actual));
            BigDecimal expected = new BigDecimal(condition.getValue());
            int cmp = current.compareTo(expected);
            return switch (condition.getOperator()) {
                case EQ -> cmp == 0;
                case NEQ -> cmp != 0;
                case GT -> cmp > 0;
                case GTE -> cmp >= 0;
                case LT -> cmp < 0;
                case LTE -> cmp <= 0;
                default -> throw new IllegalArgumentException("Unsupported numeric operator: " + condition.getOperator());
            };
        }
        throw new IllegalArgumentException("Unsupported condition type: " + condition.getValueType());
    }

    private boolean compareStrings(String actual, RuleCondition condition) {
        Set<String> values = Arrays.stream(condition.getValue().split(","))
                .map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
        String normalized = actual.toLowerCase();
        return switch (condition.getOperator()) {
            case EQ -> normalized.equals(condition.getValue().trim().toLowerCase());
            case NEQ -> !normalized.equals(condition.getValue().trim().toLowerCase());
            case IN -> values.contains(normalized);
            case NOT_IN -> !values.contains(normalized);
            default -> throw new IllegalArgumentException("Unsupported string operator: " + condition.getOperator());
        };
    }

    private String buildReason(RegulatoryRule rule) {
        return "Matched verified rule " + rule.getCode() + ": " + rule.getName();
    }

    public record AnalysisResult(
            UUID approvalId,
            String approvalCode,
            String approvalName,
            String authority,
            ApplicabilityStatus status,
            String ruleCode,
            UUID sourceId,
            String sourceTitle,
            String sourceUrl,
            String reason,
            String basis) {}
}
