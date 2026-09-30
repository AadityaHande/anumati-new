package in.anumati.platform.analytics;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/department")
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public DepartmentDashboardResponse department() {
        return service.summary();
    }
}
