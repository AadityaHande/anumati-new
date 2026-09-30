package in.anumati.platform.knowledge;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge/applications")
public class ApplicationEvidenceGraphController {
    private final ApplicationEvidenceGraphService service;

    public ApplicationEvidenceGraphController(ApplicationEvidenceGraphService service) {
        this.service = service;
    }

    @GetMapping("/{applicationId}/evidence-graph")
    @PreAuthorize("isAuthenticated()")
    public ApplicationEvidenceGraphResponse graph(@PathVariable UUID applicationId, Authentication auth) {
        boolean departmentActor = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DEPARTMENT_OFFICER"));
        return service.build(applicationId, auth.getName(), departmentActor);
    }
}
