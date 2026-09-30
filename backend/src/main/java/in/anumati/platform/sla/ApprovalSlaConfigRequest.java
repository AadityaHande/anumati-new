package in.anumati.platform.sla;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ApprovalSlaConfigRequest(
        @NotNull UUID approvalId,
        @Positive int targetHours,
        @PositiveOrZero int warningHours,
        @NotNull UUID sourceId,
        @NotNull Boolean active,
        Map<String, String> scopeAttributes,
        @PositiveOrZero BigDecimal maxInvestmentInr) {}
