package in.anumati.platform.operations;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations/inspections")
public class InspectionPlannerController {
    private final InspectionPlannerService service;

    public InspectionPlannerController(InspectionPlannerService service) {
        this.service = service;
    }

    @GetMapping("/planner")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public InspectionPlannerResponse planner() {
        return service.plan();
    }
}
