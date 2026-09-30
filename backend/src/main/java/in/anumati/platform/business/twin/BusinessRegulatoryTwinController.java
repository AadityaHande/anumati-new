package in.anumati.platform.business.twin;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-profiles/{businessProfileId}/regulatory-twin")
public class BusinessRegulatoryTwinController {
    private final BusinessRegulatoryTwinService service;
    public BusinessRegulatoryTwinController(BusinessRegulatoryTwinService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public BusinessRegulatoryTwinResponse current(@PathVariable UUID businessProfileId, Authentication auth) {
        return service.current(businessProfileId, auth.getName());
    }
}
