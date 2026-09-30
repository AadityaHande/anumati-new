package in.anumati.platform.preflight;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preflight")
public class PreflightController {
    private final PreflightService service;
    public PreflightController(PreflightService service){this.service=service;}
    @GetMapping("/business-profiles/{businessProfileId}/analysis/{analysisRunId}")
    public PreflightResponse evaluate(@PathVariable UUID businessProfileId,@PathVariable UUID analysisRunId,Authentication auth){
        return service.evaluate(businessProfileId,analysisRunId,auth.getName());
    }
}
