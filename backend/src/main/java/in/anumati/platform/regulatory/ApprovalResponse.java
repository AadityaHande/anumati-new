package in.anumati.platform.regulatory;

import java.util.UUID;

public record ApprovalResponse(UUID id, String code, String name, String authority, String purpose,
                               UUID sourceId, String sourceTitle, String sourceUrl, boolean active) {
    static ApprovalResponse from(Approval a) {
        RegulatorySource s = a.getSource();
        return new ApprovalResponse(a.getId(), a.getCode(), a.getName(), a.getAuthority(), a.getPurpose(),
                s.getId(), s.getTitle(), s.getUrl(), a.isActive());
    }
}
