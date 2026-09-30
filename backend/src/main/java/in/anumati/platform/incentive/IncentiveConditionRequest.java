package in.anumati.platform.incentive;
import in.anumati.platform.regulatory.*;
public record IncentiveConditionRequest(ConditionField field, ConditionOperator operator, ValueType valueType, String value, int sequenceNumber) {}
