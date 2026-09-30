package in.anumati.platform.knowledge;

import in.anumati.platform.application.*;
import in.anumati.platform.dependency.ApprovalDependency;
import in.anumati.platform.dependency.ApprovalDependencyRepository;
import in.anumati.platform.dependency.DependencyType;
import in.anumati.platform.document.Document;
import in.anumati.platform.document.DocumentRepository;
import in.anumati.platform.evidence.VerifiedEvidence;
import in.anumati.platform.evidence.VerifiedEvidenceRepository;
import in.anumati.platform.evidence.VerifiedEvidenceStatus;
import in.anumati.platform.regulatory.RegulatoryRuleRepository;
import in.anumati.platform.regulatory.RegulatorySource;
import in.anumati.platform.regulatory.RegulatorySourceRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ApplicationEvidenceGraphService {
    private final ApplicationRepository applications;
    private final ApplicationQueryRepository queries;
    private final InspectionRepository inspections;
    private final DecisionRepository decisions;
    private final ApprovalDependencyRepository dependencies;
    private final DocumentRepository documents;
    private final VerifiedEvidenceRepository evidence;
    private final RegulatoryRuleRepository rules;
    private final RegulatorySourceRepository sources;

    public ApplicationEvidenceGraphService(ApplicationRepository applications,
                                           ApplicationQueryRepository queries,
                                           InspectionRepository inspections,
                                           DecisionRepository decisions,
                                           ApprovalDependencyRepository dependencies,
                                           DocumentRepository documents,
                                           VerifiedEvidenceRepository evidence,
                                           RegulatoryRuleRepository rules,
                                           RegulatorySourceRepository sources) {
        this.applications = applications;
        this.queries = queries;
        this.inspections = inspections;
        this.decisions = decisions;
        this.dependencies = dependencies;
        this.documents = documents;
        this.evidence = evidence;
        this.rules = rules;
        this.sources = sources;
    }

    @Transactional(readOnly = true)
    public ApplicationEvidenceGraphResponse build(UUID applicationId, String actor, boolean departmentActor) {
        ApplicationRecord app = applications.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        if (!departmentActor && !app.getBusinessProfile().getOwnerActor().equals(actor)) {
            throw new AccessDeniedException("Not permitted");
        }

        Map<UUID, ApplicationEvidenceGraphResponse.Node> nodes = new LinkedHashMap<>();
        List<ApplicationEvidenceGraphResponse.Edge> edges = new ArrayList<>();
        UUID appNode = add(nodes, app.getId(), "APPLICATION", app.getApprovalNameSnapshot(), app.getStatus().name());
        UUID approvalNode = add(nodes, app.getApproval().getId(), "APPROVAL", app.getApprovalCodeSnapshot(), app.getAuthoritySnapshot());
        edges.add(new ApplicationEvidenceGraphResponse.Edge(appNode, approvalNode, "FOR_APPROVAL"));

        if (app.getMatchedRuleCode() != null) {
            rules.findAll().stream()
                    .filter(r -> r.getCode().equals(app.getMatchedRuleCode()))
                    .findFirst()
                    .ifPresent(rule -> {
                        UUID ruleNode = add(nodes, rule.getId(), "RULE", rule.getCode(), rule.getName());
                        edges.add(new ApplicationEvidenceGraphResponse.Edge(approvalNode, ruleNode, "DETERMINED_BY"));
                        RegulatorySource source = rule.getSource();
                        UUID sourceNode = add(nodes, source.getId(), "SOURCE", source.getTitle(), source.getUrl());
                        edges.add(new ApplicationEvidenceGraphResponse.Edge(ruleNode, sourceNode, "SUPPORTED_BY"));
                    });
        } else if (app.getSourceId() != null) {
            sources.findById(app.getSourceId()).ifPresent(source -> {
                UUID sourceNode = add(nodes, source.getId(), "SOURCE", source.getTitle(), source.getUrl());
                edges.add(new ApplicationEvidenceGraphResponse.Edge(approvalNode, sourceNode, "SUPPORTED_BY"));
            });
        }

        for (VerifiedEvidence item : evidence.findByBusinessProfile_IdAndProfileVersionAndStatusOrderByFieldName(
                app.getBusinessProfile().getId(), app.getProfileVersion(), VerifiedEvidenceStatus.VERIFIED)) {
            UUID evidenceNode = add(nodes, item.getId(), "EVIDENCE", item.getFieldName(), item.getFieldValue());
            edges.add(new ApplicationEvidenceGraphResponse.Edge(appNode, evidenceNode, "USES_EVIDENCE"));
            Document doc = item.getSourceDocument();
            UUID documentNode = add(nodes, doc.getId(), "DOCUMENT", doc.getOriginalFilename(), doc.getNormalizedCategory());
            edges.add(new ApplicationEvidenceGraphResponse.Edge(evidenceNode, documentNode, "FROM_DOCUMENT"));
        }

        for (Document doc : documents.findByBusinessProfile_IdOrderByCreatedAtDesc(app.getBusinessProfile().getId())) {
            UUID documentNode = add(nodes, doc.getId(), "DOCUMENT", doc.getOriginalFilename(), doc.getStatus().name());
            edges.add(new ApplicationEvidenceGraphResponse.Edge(appNode, documentNode, "APPLICATION_EVIDENCE"));
        }

        for (ApprovalDependency dependency : dependencies.findByApproval_IdAndActiveTrue(app.getApproval().getId())) {
            if (dependency.getDependencyType() != DependencyType.BLOCKING) continue;
            UUID dependencyNode = add(nodes, dependency.getDependsOnApproval().getId(), "DEPENDENCY", dependency.getDependsOnApproval().getCode(), dependency.getDependsOnApproval().getName());
            edges.add(new ApplicationEvidenceGraphResponse.Edge(appNode, dependencyNode, "BLOCKED_BY"));
        }

        for (ApplicationQuery query : queries.findByApplication_IdOrderByRaisedAtAsc(applicationId)) {
            UUID queryNode = add(nodes, query.getId(), "QUERY", query.getSubject(), query.getStatus().name());
            edges.add(new ApplicationEvidenceGraphResponse.Edge(appNode, queryNode, "HAS_QUERY"));
        }

        for (Inspection inspection : inspections.findByApplication_IdOrderByScheduledAtAsc(applicationId)) {
            UUID inspectionNode = add(nodes, inspection.getId(), "INSPECTION", inspection.getAssignedOfficer(), inspection.getScheduledAt().toString());
            edges.add(new ApplicationEvidenceGraphResponse.Edge(appNode, inspectionNode, "HAS_INSPECTION"));
        }

        decisions.findByApplication_Id(applicationId).ifPresent(decision -> {
            UUID decisionNode = add(nodes, decision.getId(), "DECISION", decision.getOutcome().name(), decision.getDecisionNotes());
            edges.add(new ApplicationEvidenceGraphResponse.Edge(appNode, decisionNode, "HAS_DECISION"));
            if (decision.getCitedSourceId() != null) {
                sources.findById(decision.getCitedSourceId()).ifPresent(source -> {
                    UUID sourceNode = add(nodes, source.getId(), "SOURCE", source.getTitle(), source.getUrl());
                    edges.add(new ApplicationEvidenceGraphResponse.Edge(decisionNode, sourceNode, "CITES"));
                });
            }
        });

        return new ApplicationEvidenceGraphResponse(applicationId, List.copyOf(nodes.values()), List.copyOf(edges));
    }

    private UUID add(Map<UUID, ApplicationEvidenceGraphResponse.Node> nodes, UUID id, String type, String label, String detail) {
        nodes.putIfAbsent(id, new ApplicationEvidenceGraphResponse.Node(id, type, label, detail == null ? "" : detail));
        return id;
    }
}
