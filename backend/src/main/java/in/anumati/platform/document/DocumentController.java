package in.anumati.platform.document;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentService service;
    public DocumentController(DocumentService service) { this.service = service; }

    @PostMapping("/presign")
    public PresignResponse presign(@Valid @RequestBody DocumentRequest request, Authentication authentication) {
        return service.createPresignedUpload(request, authentication.getName());
    }

    @PutMapping("/local-upload")
    public void localUpload(@RequestParam String key, HttpServletRequest request, Authentication authentication) throws IOException {
        service.uploadLocal(key, request.getInputStream(), request.getContentLengthLong(), request.getContentType(), authentication.getName());
    }

    @PostMapping("/{id}/complete")
    public DocumentResponse complete(@PathVariable UUID id, Authentication authentication) {
        return service.complete(id, authentication.getName());
    }

    @GetMapping
    public List<DocumentResponse> list(@RequestParam UUID businessProfileId, Authentication authentication) {
        return service.list(businessProfileId, authentication.getName());
    }
}
