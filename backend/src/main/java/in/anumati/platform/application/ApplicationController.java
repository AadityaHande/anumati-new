package in.anumati.platform.application;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationLifecycleService service;

    public ApplicationController(ApplicationLifecycleService service) { this.service = service; }

    private boolean departmentActor(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DEPARTMENT_OFFICER"));
    }

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicationResponse submit(@Valid @RequestBody ApplicationRequest request, Authentication auth) {
        return service.submit(request, auth.getName());
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable UUID id, Authentication auth) {
        return service.get(id, auth.getName(), departmentActor(auth));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public List<ApplicationSummaryResponse> listAll(Authentication auth) {
        return service.listForDepartment();
    }

    @GetMapping("/business-profiles/{businessProfileId}")
    @PreAuthorize("hasRole('APPLICANT')")
    public List<ApplicationSummaryResponse> list(@PathVariable UUID businessProfileId, Authentication auth) {
        return service.listForBusiness(businessProfileId, auth.getName());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER','APPLICANT')")
    public ApplicationResponse transition(@PathVariable UUID id, @Valid @RequestBody StatusTransitionRequest request,
                                          Authentication auth) {
        boolean departmentActor = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DEPARTMENT_OFFICER"));
        return service.transition(id, request, auth.getName(), departmentActor);
    }

    @PostMapping("/{id}/queries")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public ApplicationQueryResponseDto raiseQuery(@PathVariable UUID id, @Valid @RequestBody QueryRequest request,
                                                   Authentication auth) {
        return service.raiseQuery(id, request, auth.getName());
    }

    @PostMapping("/{id}/queries/{queryId}/responses")
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicationQueryResponseDto respondToQuery(@PathVariable UUID id, @PathVariable UUID queryId,
                                                       @Valid @RequestBody QueryResponseRequest request,
                                                       Authentication auth) {
        return service.respondToQuery(id, queryId, request, auth.getName(), departmentActor(auth));
    }

    @PostMapping("/{id}/inspections")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public InspectionResponse scheduleInspection(@PathVariable UUID id, @Valid @RequestBody InspectionRequest request,
                                                 Authentication auth) {
        return service.scheduleInspection(id, request, auth.getName());
    }

    @PatchMapping("/{id}/inspections/{inspectionId}")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public InspectionResponse completeInspection(@PathVariable UUID id, @PathVariable UUID inspectionId,
                                                  @Valid @RequestBody InspectionCompletionRequest request,
                                                  Authentication auth) {
        return service.completeInspection(id, inspectionId, request, auth.getName());
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public ApplicationResponse decision(@PathVariable UUID id, @Valid @RequestBody DecisionRequest request,
                                        Authentication auth) {
        return service.decide(id, request, auth.getName());
    }

    @GetMapping("/{id}/timeline")
    public ApplicationTimelineResponse timeline(@PathVariable UUID id, Authentication auth) {
        return service.timeline(id, auth.getName(), departmentActor(auth));
    }
}
