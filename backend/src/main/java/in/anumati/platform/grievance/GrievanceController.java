package in.anumati.platform.grievance;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grievances")
public class GrievanceController {
    private final GrievanceService service;

    public GrievanceController(GrievanceService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public GrievanceResponse create(@Valid @RequestBody GrievanceRequest r, Authentication a) {
        return service.create(r, a.getName());
    }

    @GetMapping("/business-profiles/{id}")
    @PreAuthorize("hasRole('APPLICANT')")
    public List<GrievanceResponse> business(@PathVariable UUID id, Authentication a) {
        return service.business(id, a.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public List<GrievanceResponse> all() {
        return service.all();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public GrievanceResponse update(@PathVariable UUID id, @RequestBody GrievanceUpdateRequest r, Authentication a) {
        return service.update(id, r, a.getName());
    }
}
