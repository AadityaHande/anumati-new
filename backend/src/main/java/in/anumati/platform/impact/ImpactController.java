package in.anumati.platform.impact;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/impact")
public class ImpactController {
 private final ImpactService service; public ImpactController(ImpactService s){service=s;}
 @PostMapping("/business-profiles/{businessProfileId}/simulate")
 public ImpactResponse simulate(@PathVariable UUID businessProfileId,@Valid @RequestBody ImpactRequest request,Authentication auth){return service.simulate(businessProfileId,request,auth.getName());}
}
