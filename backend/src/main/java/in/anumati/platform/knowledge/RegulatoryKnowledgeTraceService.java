package in.anumati.platform.knowledge;

import in.anumati.platform.dependency.ApprovalDependencyRepository;
import in.anumati.platform.readiness.DocumentRequirementRepository;
import in.anumati.platform.regulatory.ApprovalRepository;
import in.anumati.platform.regulatory.RegulatoryRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegulatoryKnowledgeTraceService {
    private final ApprovalRepository approvals;
    private final RegulatoryRuleRepository rules;
    private final DocumentRequirementRepository documents;
    private final ApprovalDependencyRepository dependencies;

    public RegulatoryKnowledgeTraceService(ApprovalRepository approvals, RegulatoryRuleRepository rules, DocumentRequirementRepository documents, ApprovalDependencyRepository dependencies) {
        this.approvals=approvals; this.rules=rules; this.documents=documents; this.dependencies=dependencies;
    }

    @Transactional(readOnly=true)
    public RegulatoryKnowledgeTraceResponse trace(UUID approvalId) {
        var approval=approvals.findById(approvalId).orElseThrow(()->new IllegalArgumentException("Approval not found: "+approvalId));
        var ruleTrace=rules.findByActiveTrue().stream().filter(r -> r.getApproval().getId().equals(approvalId)).map(r -> new RegulatoryKnowledgeTraceResponse.RuleTrace(r.getId(),r.getCode(),r.getName(),r.getOutcome().name(),r.getVersionNumber(),r.getSource().getId(),r.getSource().getTitle(),r.getSource().getUrl())).toList();
        var docTrace=documents.findByApproval_IdAndActiveTrue(approvalId).stream().sorted(java.util.Comparator.comparing(in.anumati.platform.readiness.DocumentRequirement::getDocumentName)).map(d -> new RegulatoryKnowledgeTraceResponse.DocumentTrace(d.getId(),d.getDocumentName(),d.isMandatory(),d.getSource().getId(),d.getSource().getTitle(),d.getSource().getUrl())).toList();
        var deps=dependencies.findByApproval_IdAndActiveTrue(approvalId).stream()
                .map(d -> new RegulatoryKnowledgeTraceResponse.DependencyTrace(d.getDependsOnApproval().getId(),d.getDependsOnApproval().getCode(),d.getDependsOnApproval().getName(),d.getDependencyType().name())).toList();
        return new RegulatoryKnowledgeTraceResponse(approval.getId(),approval.getCode(),approval.getName(),approval.getAuthority(),ruleTrace,docTrace,deps);
    }
}
