package in.anumati.platform.document.analysis;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentAnalysisController {
    private final DocumentAnalysisService service;
    public DocumentAnalysisController(DocumentAnalysisService service) { this.service = service; }

    @PostMapping("/{id}/analyze")
    public DocumentAnalysisResponse analyze(@PathVariable UUID id, Authentication authentication) {
        return service.analyze(id, authentication.getName());
    }

    @GetMapping("/{id}/analysis")
    public DocumentAnalysisResponse get(@PathVariable UUID id, Authentication authentication) {
        return service.get(id, authentication.getName());
    }
}
