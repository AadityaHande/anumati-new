package in.anumati.platform.evidence;
import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/business-profiles/{businessProfileId}/evidence")
public class VerifiedEvidenceController { private final VerifiedEvidenceService service; public VerifiedEvidenceController(VerifiedEvidenceService s){service=s;}
 @GetMapping("/passport") public EvidencePassportResponse passport(@PathVariable UUID businessProfileId, Authentication auth){ return service.passport(businessProfileId,auth.getName()); }
 @GetMapping public List<VerifiedEvidenceResponse> list(@PathVariable UUID businessProfileId, Authentication auth){return service.list(businessProfileId,auth.getName());}
 @PostMapping("/from-document/{documentId}") public VerifiedEvidenceResponse promote(@PathVariable UUID businessProfileId,@PathVariable UUID documentId,Authentication auth){return service.promote(businessProfileId,documentId,auth.getName());}
}
