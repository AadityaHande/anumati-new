package in.anumati.platform.incentive;
import in.anumati.platform.regulatory.RegulatorySourceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/incentives") public class IncentiveController {
 private final IncentiveService service; private final RegulatorySourceRepository sources;
 public IncentiveController(IncentiveService s, RegulatorySourceRepository r){service=s;sources=r;}
 @PostMapping("/admin/schemes") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> create(@jakarta.validation.Valid @RequestBody IncentiveSchemeRequest request,Authentication auth){var s=service.create(request,auth.getName(),sources);return Map.of("id",s.getId(),"code",s.getCode());}
 @GetMapping("/business-profiles/{businessProfileId}") @PreAuthorize("hasRole('APPLICANT')") public List<IncentiveMatchResponse> matches(@PathVariable UUID businessProfileId,Authentication a){return service.matches(businessProfileId,a.getName());}
}
