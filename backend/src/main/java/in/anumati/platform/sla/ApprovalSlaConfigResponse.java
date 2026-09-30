package in.anumati.platform.sla;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ApprovalSlaConfigResponse(
        UUID id,
        UUID approvalId,
        String approvalCode,
        String approvalName,
        int targetHours,
        int warningHours,
        UUID sourceId,
        String sourceTitle,
        boolean active,
        Map<String, String> scopeAttributes,
        BigDecimal maxInvestmentInr,
        Instant updatedAt) {
    static ApprovalSlaConfigResponse from(ApprovalSlaConfig config) {
        return new ApprovalSlaConfigResponse(
                config.getId(), config.getApproval().getId(), config.getApproval().getCode(), config.getApproval().getName(),
                config.getTargetHours(), config.getWarningHours(), config.getSource().getId(), config.getSource().getTitle(),
                config.isActive(), config.getScopeAttributes(), config.getMaxInvestmentInr(), config.getUpdatedAt());
    }
}
