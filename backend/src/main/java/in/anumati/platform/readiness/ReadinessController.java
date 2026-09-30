package in.anumati.platform.readiness;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/readiness")
public class ReadinessController {
    private final DocumentReadinessService service;

    public ReadinessController(DocumentReadinessService service) { this.service = service; }

    @GetMapping
    public ReadinessResponse get(@RequestParam UUID businessProfileId,
                                 @RequestParam UUID analysisRunId,
                                 Authentication authentication) {
        return service.evaluate(businessProfileId, analysisRunId, authentication.getName());
    }
}
