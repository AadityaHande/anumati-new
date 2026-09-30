package in.anumati.platform.knowledge;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge")
public class RegulatoryKnowledgeTraceController {
    private final RegulatoryKnowledgeTraceService service;
    public RegulatoryKnowledgeTraceController(RegulatoryKnowledgeTraceService service){this.service=service;}

    @GetMapping("/approvals/{approvalId}/trace")
    @PreAuthorize("hasAnyRole('APPLICANT','ADMIN','DEPARTMENT_OFFICER')")
    public RegulatoryKnowledgeTraceResponse trace(@PathVariable UUID approvalId){return service.trace(approvalId);}
}
