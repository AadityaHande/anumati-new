package in.anumati.platform.regulatory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RuleConditionRequest(
        @NotNull ConditionField field,
        @NotNull ConditionOperator operator,
        @NotNull ValueType valueType,
        @NotBlank @Size(max = 500) String value,
        @NotNull @Positive Integer sequenceNumber
) {}
