package in.anumati.platform.sla;

import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.business.BusinessProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/sla")
public class SlaController {
    private final SlaService service;
    private final ApplicationRepository applications;
    private final BusinessProfileService profiles;

    public SlaController(SlaService service, ApplicationRepository applications, BusinessProfileService profiles) {
        this.service = service;
        this.applications = applications;
        this.profiles = profiles;
    }

    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public SlaResponse application(@PathVariable UUID applicationId, Authentication auth) {
        var application = applications.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        boolean departmentActor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DEPARTMENT_OFFICER"));
        if (!departmentActor) profiles.getForActor(application.getBusinessProfile().getId(), auth.getName());
        return service.status(application);
    }

    @GetMapping("/configs")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public List<ApprovalSlaConfigResponse> configs() {
        return service.list();
    }

    @PostMapping("/configs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApprovalSlaConfigResponse create(@jakarta.validation.Valid @RequestBody ApprovalSlaConfigRequest request, Authentication auth) {
        return service.upsert(request, auth.getName());
    }
}
