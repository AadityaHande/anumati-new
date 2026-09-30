package in.anumati.platform.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.anumati.platform.analysis.AnalysisRun;
import in.anumati.platform.analysis.AnalysisRunRepository;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.dependency.ApprovalDependency;
import in.anumati.platform.dependency.ApprovalDependencyRepository;
import in.anumati.platform.dependency.DependencyType;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.document.DocumentRepository;
import in.anumati.platform.readiness.DocumentReadinessService;
import in.anumati.platform.readiness.ReadinessResponse;
import in.anumati.platform.regulatory.ApplicabilityStatus;
import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.regulatory.ApprovalRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import in.anumati.platform.application.events.ApplicationSubmittedEvent;
import in.anumati.platform.application.events.ApplicationQueryRaisedEvent;
import in.anumati.platform.application.events.ApplicationQueryRespondedEvent;
import in.anumati.platform.application.events.ApplicationDecisionRecordedEvent;
import in.anumati.platform.application.events.InspectionScheduledEvent;
import in.anumati.platform.common.web.ConflictException;

import java.util.*;

@Service
public class ApplicationLifecycleService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationStateMachine stateMachine;
    private final ApprovalDependencyRepository dependencyRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final ApplicationQueryRepository queryRepository;
    private final ApplicationQueryResponseRepository queryResponseRepository;
    private final InspectionRepository inspectionRepository;
    private final DecisionRepository decisionRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final ApprovalRepository approvalRepository;
    private final BusinessProfileService profileService;
    private final DocumentReadinessService readinessService;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public ApplicationLifecycleService(ApplicationRepository applicationRepository,
                                       ApplicationStateMachine stateMachine,
                                       ApprovalDependencyRepository dependencyRepository,
                                       ApplicationStatusHistoryRepository statusHistoryRepository,
                                       ApplicationQueryRepository queryRepository,
                                       ApplicationQueryResponseRepository queryResponseRepository,
                                       InspectionRepository inspectionRepository,
                                       DecisionRepository decisionRepository,
                                       AnalysisRunRepository analysisRunRepository,
                                       ApprovalRepository approvalRepository,
                                       BusinessProfileService profileService,
                                       DocumentReadinessService readinessService,
                                       DocumentRepository documentRepository,
                                       ObjectMapper objectMapper,
                                       AuditService auditService,
                                       ApplicationEventPublisher eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.stateMachine = stateMachine;
        this.dependencyRepository = dependencyRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.queryRepository = queryRepository;
        this.queryResponseRepository = queryResponseRepository;
        this.inspectionRepository = inspectionRepository;
        this.decisionRepository = decisionRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.approvalRepository = approvalRepository;
        this.profileService = profileService;
        this.readinessService = readinessService;
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ApplicationResponse submit(ApplicationRequest request, String actor) {
        BusinessProfile profile = profileService.getForActor(request.businessProfileId(), actor);
        AnalysisRun run = analysisRunRepository.findById(request.analysisRunId())
                .orElseThrow(() -> new IllegalArgumentException("Analysis run not found: " + request.analysisRunId()));
        if (!run.getBusinessProfileId().equals(profile.getId())) {
            throw new IllegalArgumentException("Analysis run does not belong to the business profile");
        }
        if (!Objects.equals(run.getProfileVersion(), profile.getVersionNumber())) {
            throw new ConflictException("Analysis run is stale for the current business profile version; re-run regulatory analysis before submitting");
        }
        Approval approval = approvalRepository.findById(request.approvalId())
                .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + request.approvalId()));

        Map<String, Object> result = resultForApproval(run, approval.getId());
        String status = String.valueOf(result.get("status"));
        if (!(ApplicabilityStatus.APPLICABLE.name().equals(status) || ApplicabilityStatus.CONDITIONAL.name().equals(status))) {
            throw new IllegalArgumentException("This approval is not applicable or conditional for the selected analysis run");
        }

        ReadinessResponse readiness = readinessService.evaluate(profile.getId(), run.getId(), actor);
        ReadinessResponse.ApprovalReadiness approvalReadiness = readiness.approvals().stream()
                .filter(item -> approval.getId().equals(item.approvalId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No document readiness configuration exists for this approval"));
        if (!"READY".equals(approvalReadiness.status())) {
            throw new IllegalArgumentException("Application cannot be submitted until mandatory documents are ready");
        }
        ensureBlockingDependenciesReady(run, approval, actor);

        if (applicationRepository.existsByBusinessProfile_IdAndApproval_IdAndAnalysisRunIdAndStatusNotIn(
                profile.getId(), approval.getId(), run.getId(), List.of(ApplicationStatus.REJECTED, ApplicationStatus.RENEWAL_DUE))) {
            throw new ConflictException("An active application already exists for this approval and analysis run");
        }

        String reference = "ANU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        ApplicationRecord application = applicationRepository.save(new ApplicationRecord(
                profile, approval, run.getId(), run.getProfileVersion(), approval.getCode(), approval.getName(),
                approval.getAuthority(), nullableString(result.get("ruleCode")), parseUuid(result.get("sourceId")), reference));
        recordTransition(application, null, ApplicationStatus.SUBMITTED, actor, "Application submitted after readiness validation");
        eventPublisher.publishEvent(new ApplicationSubmittedEvent(application.getId(), profile.getId(), approval.getId(), profile.getOwnerActor(), application.getSubmittedAt(), actor));
        auditService.record(actor, "APPLICATION_SUBMITTED", "Application", application.getId(),
                Map.of("businessProfileId", profile.getId(), "approvalId", approval.getId(), "analysisRunId", run.getId()));
        return getResponse(application);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse get(UUID id, String actor, boolean departmentActor) {
        ApplicationRecord app = departmentActor ? loadForDepartment(id) : loadForApplicant(id, actor);
        return getResponse(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationSummaryResponse> listForBusiness(UUID businessProfileId, String actor) {
        profileService.getForActor(businessProfileId, actor);
        return applicationRepository.findByBusinessProfile_IdOrderByCreatedAtDesc(businessProfileId)
                .stream().map(ApplicationSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationSummaryResponse> listForDepartment() {
        return applicationRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(ApplicationSummaryResponse::from).toList();
    }

    @Transactional
    public ApplicationResponse transition(UUID id, StatusTransitionRequest request, String actor, boolean departmentActor) {
        ApplicationRecord app = departmentActor ? loadForDepartment(id) : loadForApplicant(id, actor);
        requireRole(departmentActor);
        ApplicationStatus target = request.targetStatus();
        validateTransition(app, target, actor, departmentActor);
        ApplicationStatus previous = app.getStatus();
        app.transitionTo(target);
        applicationRepository.save(app);
        recordTransition(app, previous, target, actor, request.reason());
        auditService.record(actor, "APPLICATION_STATUS_CHANGED", "Application", id,
                Map.of("from", previous.name(), "to", target.name()));
        return getResponse(app);
    }

    @Transactional
    public ApplicationQueryResponseDto raiseQuery(UUID id, QueryRequest request, String actor) {
        ApplicationRecord app = loadForDepartment(id);
        if (!app.getStatus().equals(ApplicationStatus.UNDER_SCRUTINY) && !app.getStatus().equals(ApplicationStatus.RESUBMITTED)) {
            throw new IllegalArgumentException("Queries can be raised only during scrutiny");
        }
        ApplicationQuery query = queryRepository.save(new ApplicationQuery(app, request.subject(), request.queryText(), actor));
        transitionInternal(app, ApplicationStatus.QUERY_RAISED, actor, "Department query raised");
        auditService.record(actor, "APPLICATION_QUERY_RAISED", "ApplicationQuery", query.getId(), Map.of("applicationId", id));
        eventPublisher.publishEvent(new ApplicationQueryRaisedEvent(id, app.getBusinessProfile().getId(), app.getBusinessProfile().getOwnerActor(), request.subject(), actor));
        return ApplicationQueryResponseDto.from(query, List.of());
    }

    @Transactional
    public ApplicationQueryResponseDto respondToQuery(UUID id, UUID queryId, QueryResponseRequest request, String actor, boolean departmentActor) {
        ApplicationRecord app;
        if (departmentActor) {
            throw new AccessDeniedException("Only the applicant can respond to an applicant query");
        }
        app = loadForApplicant(id, actor);
        ApplicationQuery query = queryRepository.findById(queryId)
                .orElseThrow(() -> new IllegalArgumentException("Query not found: " + queryId));
        if (!query.getApplication().getId().equals(id)) throw new IllegalArgumentException("Query does not belong to the application");
        if (query.getStatus() != QueryStatus.OPEN) throw new IllegalArgumentException("Query is not open");

        List<UUID> attachments = request.attachmentDocumentIds() == null ? List.of() : request.attachmentDocumentIds();
        for (UUID documentId : attachments) {
            documentRepository.findById(documentId).filter(d -> d.getBusinessProfile().getId().equals(app.getBusinessProfile().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Attachment document does not belong to this business profile: " + documentId));
        }
        String attachmentJson;
        try { attachmentJson = objectMapper.writeValueAsString(attachments); }
        catch (Exception e) { throw new IllegalStateException("Could not encode query attachments", e); }

        queryResponseRepository.save(new ApplicationQueryResponse(query, request.responseText(), actor, attachmentJson));
        query.markResponded();
        queryRepository.save(query);
        boolean openQueriesRemain = queryRepository.findByApplication_IdOrderByRaisedAtAsc(id).stream()
                .anyMatch(existing -> existing.getStatus() == QueryStatus.OPEN);
        if (!openQueriesRemain) {
            transitionInternal(app, ApplicationStatus.RESUBMITTED, actor, "Applicant responded to all open department queries");
        }
        auditService.record(actor, "APPLICATION_QUERY_RESPONDED", "ApplicationQuery", queryId, Map.of("applicationId", id));
        eventPublisher.publishEvent(new ApplicationQueryRespondedEvent(id, queryId, app.getBusinessProfile().getId(), query.getRaisedBy(), actor));
        return ApplicationQueryResponseDto.from(query, queryResponseRepository.findByQuery_IdOrderByCreatedAtAsc(queryId));
    }

    @Transactional
    public InspectionResponse scheduleInspection(UUID id, InspectionRequest request, String actor) {
        ApplicationRecord app = loadForDepartment(id);
        if (!(app.getStatus() == ApplicationStatus.UNDER_SCRUTINY || app.getStatus() == ApplicationStatus.RESUBMITTED)) {
            throw new IllegalArgumentException("Inspection can be scheduled only during scrutiny");
        }
        Inspection inspection = inspectionRepository.save(new Inspection(app, request.scheduledAt(), request.assignedOfficer()));
        transitionInternal(app, ApplicationStatus.INSPECTION_SCHEDULED, actor, "Inspection scheduled");
        auditService.record(actor, "INSPECTION_SCHEDULED", "Inspection", inspection.getId(), Map.of("applicationId", id));
        eventPublisher.publishEvent(new InspectionScheduledEvent(id, inspection.getId(), app.getBusinessProfile().getId(), app.getBusinessProfile().getOwnerActor(), inspection.getScheduledAt(), actor));
        return InspectionResponse.from(inspection);
    }

    @Transactional
    public InspectionResponse completeInspection(UUID id, UUID inspectionId, InspectionCompletionRequest request, String actor) {
        ApplicationRecord app = loadForDepartment(id);
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found: " + inspectionId));
        if (!inspection.getApplication().getId().equals(id)) throw new IllegalArgumentException("Inspection does not belong to the application");
        if (app.getStatus() != ApplicationStatus.INSPECTION_SCHEDULED) throw new IllegalArgumentException("Application is not awaiting inspection completion");
        inspection.complete(request.outcome(), request.outcomeNotes());
        inspectionRepository.save(inspection);
        transitionInternal(app, ApplicationStatus.INSPECTION_COMPLETE, actor, "Inspection completed");
        auditService.record(actor, "INSPECTION_COMPLETED", "Inspection", inspectionId, Map.of("applicationId", id, "outcome", request.outcome().name()));
        return InspectionResponse.from(inspection);
    }

    @Transactional
    public ApplicationResponse decide(UUID id, DecisionRequest request, String actor) {
        ApplicationRecord app = loadForDepartment(id);
        if (!(app.getStatus() == ApplicationStatus.UNDER_SCRUTINY || app.getStatus() == ApplicationStatus.INSPECTION_COMPLETE)) {
            throw new IllegalArgumentException("Decision can be recorded only after scrutiny or completed inspection");
        }
        if (decisionRepository.findByApplication_Id(id).isPresent()) throw new IllegalArgumentException("A decision is already recorded for this application");
        if (request.validFrom() != null && request.validUntil() != null && request.validUntil().isBefore(request.validFrom())) {
            throw new IllegalArgumentException("validUntil must be on or after validFrom");
        }
        if (request.outcome() == DecisionOutcome.APPROVED && request.validUntil() != null && request.validUntil().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("An approved decision cannot expire before today");
        }
        if (request.outcome() == DecisionOutcome.REJECTED && (request.validFrom() != null || request.validUntil() != null)) {
            throw new IllegalArgumentException("Rejected decisions cannot carry validity dates");
        }
        if (request.outcome() == DecisionOutcome.REJECTED && (request.citedRuleCode() == null || request.citedRuleCode().isBlank() || request.citedSourceId() == null)) {
            throw new IllegalArgumentException("A rejection decision requires a cited rule and source");
        }
        if (request.citedRuleCode() != null || request.citedSourceId() != null) {
            validateCitation(app, request.citedRuleCode(), request.citedSourceId());
        }
        transitionInternal(app, ApplicationStatus.DECISION, actor, "Decision recorded");
        decisionRepository.save(new Decision(app, request.outcome(), request.decisionNotes(), actor, request.citedRuleCode(), request.citedSourceId(), request.validFrom(), request.validUntil()));
        ApplicationStatus finalStatus = request.outcome() == DecisionOutcome.REJECTED ? ApplicationStatus.REJECTED : ApplicationStatus.APPROVED;
        transitionInternal(app, finalStatus, actor, request.outcome().name() + " decision");
        auditService.record(actor, "APPLICATION_DECISION_RECORDED", "Application", id,
                Map.of("outcome", request.outcome().name()));
        eventPublisher.publishEvent(new ApplicationDecisionRecordedEvent(id, app.getBusinessProfile().getId(), app.getApproval().getId(), request.outcome(), app.getBusinessProfile().getOwnerActor(), request.validFrom(), request.validUntil(), request.citedSourceId() != null ? request.citedSourceId() : app.getSourceId(), actor));
        return getResponse(app);
    }

    @Transactional(readOnly = true)
    public ApplicationTimelineResponse timeline(UUID id, String actor, boolean departmentActor) {
        ApplicationRecord app = departmentActor ? loadForDepartment(id) : loadForApplicant(id, actor);
        List<ApplicationStatusHistory> history = statusHistoryRepository.findByApplication_IdOrderByCreatedAtAsc(id);
        List<ApplicationQuery> queries = queryRepository.findByApplication_IdOrderByRaisedAtAsc(id);
        List<Inspection> inspections = inspectionRepository.findByApplication_IdOrderByScheduledAtAsc(id);
        Optional<Decision> decision = decisionRepository.findByApplication_Id(id);
        return new ApplicationTimelineResponse(
                app.getId(), app.getStatus(),
                history.stream().map(ApplicationTimelineResponse.StatusEvent::from).toList(),
                queries.stream().map(q -> ApplicationQueryResponseDto.from(q, queryResponseRepository.findByQuery_IdOrderByCreatedAtAsc(q.getId()))).toList(),
                inspections.stream().map(InspectionResponse::from).toList(),
                decision.map(DecisionResponse::from).orElse(null));
    }

    private void validateTransition(ApplicationRecord app, ApplicationStatus target, String actor, boolean departmentActor) {
        ApplicationStatus current = app.getStatus();
        if (target == current) throw new IllegalArgumentException("Application is already in status " + target);
        if (target == ApplicationStatus.SUBMITTED || target == ApplicationStatus.RESUBMITTED) {
            if (departmentActor) throw new AccessDeniedException("Only the applicant can submit or resubmit an application");
        } else if (!departmentActor) {
            throw new AccessDeniedException("Only a department officer or admin can perform this transition");
        }
        if (!stateMachine.isAllowed(current, target)) {
            throw new IllegalArgumentException("Illegal application transition: " + current + " -> " + target);
        }
        if (target == ApplicationStatus.DECISION && decisionRepository.findByApplication_Id(app.getId()).isPresent()) {
            throw new IllegalArgumentException("A decision is already recorded");
        }
    }

    private void transitionInternal(ApplicationRecord app, ApplicationStatus target, String actor, String reason) {
        ApplicationStatus previous = app.getStatus();
        if (!stateMachine.isAllowed(previous, target)) {
            throw new IllegalArgumentException("Illegal application transition: " + previous + " -> " + target);
        }
        app.transitionTo(target);
        applicationRepository.save(app);
        recordTransition(app, previous, target, actor, reason);
    }

    private void recordTransition(ApplicationRecord app, ApplicationStatus from, ApplicationStatus to, String actor, String reason) {
        statusHistoryRepository.save(new ApplicationStatusHistory(app, from, to, actor, reason));
    }

    private ApplicationRecord loadForApplicant(UUID id, String actor) {
        ApplicationRecord app = load(id);
        profileService.getForActor(app.getBusinessProfile().getId(), actor);
        return app;
    }

    private ApplicationRecord loadForDepartment(UUID id) {
        return load(id);
    }

    private ApplicationRecord load(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    private ApplicationResponse getResponse(ApplicationRecord app) {
        List<ApplicationStatusHistory> history = statusHistoryRepository.findByApplication_IdOrderByCreatedAtAsc(app.getId());
        return ApplicationResponse.from(app, history);
    }

    private void requireRole(boolean departmentActor) {
        if (!departmentActor) throw new AccessDeniedException("Department officer role required");
    }

    private void ensureBlockingDependenciesReady(AnalysisRun run, Approval approval, String actor) {
        List<Map<String, Object>> results = objectMapper.convertValue(run.getResultSnapshot().getOrDefault("results", List.of()), new TypeReference<>() {});
        Map<UUID, Map<String, Object>> byApproval = results.stream()
                .filter(item -> item.get("approvalId") != null)
                .collect(java.util.stream.Collectors.toMap(item -> UUID.fromString(String.valueOf(item.get("approvalId"))), item -> item, (a, b) -> a));
        for (ApprovalDependency dependency : dependencyRepository.findByApproval_IdAndActiveTrue(approval.getId())) {
            if (dependency.getDependencyType() != DependencyType.BLOCKING) continue;
            Map<String, Object> dependencyResult = byApproval.get(dependency.getDependsOnApproval().getId());
            if (dependencyResult == null) continue;
            String dependencyStatus = String.valueOf(dependencyResult.get("status"));
            if (ApplicabilityStatus.NOT_APPLICABLE.name().equals(dependencyStatus)) continue;
            if (!(ApplicabilityStatus.APPLICABLE.name().equals(dependencyStatus) || ApplicabilityStatus.CONDITIONAL.name().equals(dependencyStatus))) continue;
            ReadinessResponse readiness = readinessService.evaluate(run.getBusinessProfileId(), run.getId(), actor);
            boolean ready = readiness.approvals().stream()
                    .filter(item -> dependency.getDependsOnApproval().getId().equals(item.approvalId()))
                    .anyMatch(item -> "READY".equals(item.status()));
            if (!ready) {
                throw new IllegalArgumentException("Blocking dependency is not ready: " + dependency.getDependsOnApproval().getCode());
            }
        }
    }

    private Map<String, Object> resultForApproval(AnalysisRun run, UUID approvalId) {
        List<Map<String, Object>> results = objectMapper.convertValue(run.getResultSnapshot().getOrDefault("results", List.of()), new TypeReference<>() {});
        return results.stream().filter(item -> approvalId.toString().equals(String.valueOf(item.get("approvalId"))))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Approval was not part of this analysis run"));
    }

    private void validateCitation(ApplicationRecord app, String ruleCode, UUID sourceId) {
        if (ruleCode != null && !ruleCode.isBlank() && !ruleCode.equals(app.getMatchedRuleCode())) {
            throw new IllegalArgumentException("Cited rule does not match the rule recorded for this application");
        }
        if (sourceId != null && !sourceId.equals(app.getSourceId())) {
            throw new IllegalArgumentException("Cited source does not match the source recorded for this application");
        }
    }

    private static String nullableString(Object value) { return value == null ? null : String.valueOf(value); }
    private static UUID parseUuid(Object value) { return value == null ? null : UUID.fromString(String.valueOf(value)); }
}
