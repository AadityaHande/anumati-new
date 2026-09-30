package in.anumati.platform.analysis;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {
    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping("/business-profiles/{businessProfileId}")
    public AnalysisRunResponse analyse(@PathVariable UUID businessProfileId, Authentication authentication) {
        return service.analyse(businessProfileId, authentication.getName());
    }

    @GetMapping("/business-profiles/{businessProfileId}/latest")
    public AnalysisRunResponse latest(@PathVariable UUID businessProfileId, Authentication authentication) {
        return service.latest(businessProfileId, authentication.getName());
    }

    @GetMapping("/{analysisRunId}")
    public AnalysisRunResponse get(@PathVariable UUID analysisRunId, Authentication authentication) {
        return service.get(analysisRunId, authentication.getName());
    }
}
