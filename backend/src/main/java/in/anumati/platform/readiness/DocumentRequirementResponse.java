package in.anumati.platform.readiness;

import java.util.UUID;

public record DocumentRequirementResponse(
        UUID id,
        UUID approvalId,
        String approvalCode,
        String category,
        String documentName,
        String description,
        boolean mandatory,
        boolean active,
        UUID sourceId) {
    public static DocumentRequirementResponse from(DocumentRequirement r) {
        return new DocumentRequirementResponse(
                r.getId(), r.getApproval().getId(), r.getApproval().getCode(), r.getCategory(),
                r.getDocumentName(), r.getDescription(), r.isMandatory(), r.isActive(),
                r.getSource() == null ? null : r.getSource().getId());
    }
}
