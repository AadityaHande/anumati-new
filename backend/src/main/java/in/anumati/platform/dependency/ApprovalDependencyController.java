package in.anumati.platform.dependency;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regulatory/dependencies")
public class ApprovalDependencyController {
    private final ApprovalDependencyService service;

    public ApprovalDependencyController(ApprovalDependencyService service) { this.service = service; }

    @GetMapping
    public List<ApprovalDependencyResponse> list() { return service.list(); }

    @GetMapping("/graph")
    public ApprovalDependencyService.Graph graph() { return service.buildGraph(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApprovalDependencyResponse create(@Valid @RequestBody ApprovalDependencyRequest request,
                                             Authentication auth) {
        return ApprovalDependencyResponse.from(service.create(request, auth.getName()));
    }
}
