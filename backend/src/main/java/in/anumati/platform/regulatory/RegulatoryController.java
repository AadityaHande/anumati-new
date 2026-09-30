package in.anumati.platform.regulatory;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regulatory")
public class RegulatoryController {
    private final RegulatoryAdminService service;

    public RegulatoryController(RegulatoryAdminService service) {
        this.service = service;
    }

    @GetMapping("/approvals")
    public List<ApprovalResponse> approvals() {
        return service.listApprovals().stream().map(ApprovalResponse::from).toList();
    }

    @PostMapping("/admin/sources")
    @PreAuthorize("hasRole('ADMIN')")
    public RegulatorySourceResponse createSource(@Valid @RequestBody SourceRequest request, Authentication auth) {
        return RegulatorySourceResponse.from(service.createSource(request, auth.getName()));
    }

    @PostMapping("/admin/approvals")
    @PreAuthorize("hasRole('ADMIN')")
    public ApprovalResponse createApproval(@Valid @RequestBody ApprovalRequest request, Authentication auth) {
        return ApprovalResponse.from(service.createApproval(request, auth.getName()));
    }

    @PostMapping("/admin/rules")
    @PreAuthorize("hasRole('ADMIN')")
    public RegulatoryRule createRule(@Valid @RequestBody RuleRequest request, Authentication auth) {
        return service.createRule(request, auth.getName());
    }
}
