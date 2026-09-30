package in.anumati.platform.readiness;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regulatory/document-requirements")
public class DocumentRequirementController {
    private final DocumentRequirementRepository repository;
    private final DocumentRequirementService service;

    public DocumentRequirementController(DocumentRequirementRepository repository, DocumentRequirementService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public List<DocumentRequirementResponse> list() {
        return repository.findByActiveTrue().stream().map(DocumentRequirementResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DocumentRequirementResponse create(@Valid @RequestBody DocumentRequirementRequest request,
                                              Authentication auth) {
        return DocumentRequirementResponse.from(service.create(request, auth.getName()));
    }
}
