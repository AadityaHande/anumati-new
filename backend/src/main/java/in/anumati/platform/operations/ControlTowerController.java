package in.anumati.platform.operations;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations/control-tower")
public class ControlTowerController {
    private final ControlTowerService service;

    public ControlTowerController(ControlTowerService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_OFFICER')")
    public ControlTowerResponse get() {
        return service.summary();
    }
}
