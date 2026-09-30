package in.anumati.platform.business;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/business-profiles")
public class BusinessProfileController {
    private final BusinessProfileVersionRepository versionRepository;
    private final BusinessProfileService service;

    public BusinessProfileController(BusinessProfileService service, BusinessProfileVersionRepository versionRepository) {
        this.service = service; this.versionRepository = versionRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public BusinessProfileResponse create(@Valid @RequestBody BusinessProfileRequest request,
                                          Authentication authentication) {
        return service.create(request, authentication.getName());
    }

    @GetMapping
    public java.util.List<BusinessProfileResponse> list(Authentication authentication) {
        return service.listForActor(authentication.getName());
    }

    @GetMapping("/{id}/versions")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public java.util.List<BusinessProfileVersionResponse> versions(@PathVariable UUID id, Authentication auth) {
        service.getForActor(id, auth.getName());
        return versionRepository.findByBusinessProfileIdOrderByVersionNumberDesc(id).stream().map(BusinessProfileVersionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BusinessProfileResponse get(@PathVariable UUID id, Authentication authentication) {
        return service.getResponse(id, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('APPLICANT')")
    public BusinessProfileResponse update(@PathVariable UUID id, @Valid @RequestBody BusinessProfileRequest request,
                                          Authentication authentication) {
        return service.update(id, request, authentication.getName());
    }
}
