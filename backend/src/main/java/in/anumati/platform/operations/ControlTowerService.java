package in.anumati.platform.operations;

import in.anumati.platform.application.*;
import in.anumati.platform.analysis.AnalysisRun;
import in.anumati.platform.analysis.AnalysisRunRepository;
import in.anumati.platform.dependency.ApprovalDependency;
import in.anumati.platform.dependency.ApprovalDependencyRepository;
import in.anumati.platform.dependency.DependencyType;
import in.anumati.platform.sla.SlaResponse;
import in.anumati.platform.sla.SlaService;
import in.anumati.platform.sla.SlaStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ControlTowerService {
    private final ApplicationRepository applications;
    private final ApprovalDependencyRepository dependencies;
    private final SlaService slaService;
    private final AnalysisRunRepository analyses;

    public ControlTowerService(ApplicationRepository applications,
                               ApprovalDependencyRepository dependencies,
                               SlaService slaService,
                               AnalysisRunRepository analyses) {
        this.applications = applications;
        this.dependencies = dependencies;
        this.slaService = slaService;
        this.analyses = analyses;
    }

    /**
     * Builds a complete operational snapshot using bulk-loaded SLA and analysis data
     * and indexed dependency checks. The prototype intentionally returns one snapshot;
     * statewide deployments should paginate lanes and move aggregate counts into SQL/views.
     */
    @Transactional(readOnly = true)
    public ControlTowerResponse summary() {
        List<ApplicationRecord> all = applications.findAllByOrderByCreatedAtDesc();
        List<ApprovalDependency> blocking = dependencies.findByActiveTrue().stream()
                .filter(d -> d.getDependencyType() == DependencyType.BLOCKING)
                .toList();

        java.util.Map<UUID, SlaResponse> slaByApplication = slaService.statuses(all);
        java.util.Map<UUID, AnalysisRun> analysisById = new HashMap<>();
        List<UUID> analysisIds = all.stream().map(ApplicationRecord::getAnalysisRunId).filter(Objects::nonNull).distinct().toList();
        analyses.findAllById(analysisIds).forEach(run -> analysisById.put(run.getId(), run));

        Map<UUID, List<ApprovalDependency>> blockersByApproval = blocking.stream()
                .collect(java.util.stream.Collectors.groupingBy(d -> d.getApproval().getId()));
        Set<BusinessApprovalKey> approvedApplications = all.stream()
                .filter(candidate -> candidate.getStatus() == ApplicationStatus.APPROVED)
                .map(candidate -> new BusinessApprovalKey(candidate.getBusinessProfile().getId(), candidate.getApproval().getId()))
                .collect(java.util.stream.Collectors.toSet());

        int atRisk = 0;
        int overdue = 0;
        int blocked = 0;
        List<ControlTowerResponse.Lane> lanes = new ArrayList<>(all.size());

        for (ApplicationRecord application : all) {
            SlaResponse sla = slaByApplication.get(application.getId());
            if (sla.status() == SlaStatus.AT_RISK) atRisk++;
            if (sla.status() == SlaStatus.OVERDUE) overdue++;

            List<String> blockers = findBlockers(application, approvedApplications, blockersByApproval.getOrDefault(application.getApproval().getId(), List.of()),
                    analysisById.get(application.getAnalysisRunId()));
            if (!blockers.isEmpty()) blocked++;

            lanes.add(new ControlTowerResponse.Lane(
                    application.getId(),
                    application.getExternalReference() == null ? application.getId().toString() : application.getExternalReference(),
                    application.getApprovalCodeSnapshot(),
                    application.getApprovalNameSnapshot(),
                    application.getAuthoritySnapshot(),
                    application.getBusinessProfile().getBusinessName(),
                    application.getBusinessProfile().getDistrict(),
                    application.getStatus().name(),
                    sla.status().name(),
                    sla.dueAt(),
                    blockers,
                    nextAction(application, blockers, sla.status()),
                    application.getUpdatedAt()));
        }

        return new ControlTowerResponse(all.size(), atRisk, overdue, blocked, lanes);
    }

    private List<String> findBlockers(ApplicationRecord current,
                                      Set<BusinessApprovalKey> approvedApplications,
                                      List<ApprovalDependency> dependencies,
                                      AnalysisRun analysisRun) {
        if (current.getStatus() == ApplicationStatus.APPROVED
                || current.getStatus() == ApplicationStatus.REJECTED
                || current.getStatus() == ApplicationStatus.RENEWAL_DUE) {
            return List.of();
        }
        Set<UUID> notApplicable = latestNotApplicableApprovals(analysisRun);
        List<String> blockers = new ArrayList<>();
        for (ApprovalDependency dependency : dependencies) {
            UUID dependsOnId = dependency.getDependsOnApproval().getId();
            if (notApplicable.contains(dependsOnId)) continue;
            boolean satisfied = approvedApplications.contains(
                    new BusinessApprovalKey(current.getBusinessProfile().getId(), dependsOnId));
            if (!satisfied) blockers.add(dependency.getDependsOnApproval().getCode());
        }
        return blockers;
    }

    private Set<UUID> latestNotApplicableApprovals(AnalysisRun run) {
        if (run == null || run.getResultSnapshot() == null) return Set.of();
        Object raw = run.getResultSnapshot().get("results");
        if (!(raw instanceof Collection<?> collection)) return Set.of();
        Set<UUID> result = new HashSet<>();
        for (Object value : collection) {
            if (!(value instanceof Map<?, ?> map)) continue;
            if (!"NOT_APPLICABLE".equals(String.valueOf(map.get("status")))) continue;
            Object approvalId = map.get("approvalId");
            try { result.add(UUID.fromString(String.valueOf(approvalId))); } catch (IllegalArgumentException ignored) { }
        }
        return result;
    }

    private record BusinessApprovalKey(UUID businessProfileId, UUID approvalId) {}

    private String nextAction(ApplicationRecord application, List<String> blockers, SlaStatus slaStatus) {
        if (!blockers.isEmpty()) return "Resolve blocking approval dependency";
        if (slaStatus == SlaStatus.OVERDUE) return "Escalate service timeline";
        if (slaStatus == SlaStatus.AT_RISK) return "Review service timeline";
        return switch (application.getStatus()) {
            case SUBMITTED -> "Start scrutiny";
            case UNDER_SCRUTINY -> "Review evidence or raise a query";
            case QUERY_RAISED -> "Await applicant response";
            case RESUBMITTED -> "Review response";
            case INSPECTION_SCHEDULED -> "Complete scheduled inspection";
            case INSPECTION_COMPLETE -> "Record decision";
            case DECISION -> "Complete decision processing";
            case APPROVED -> "Monitor compliance and renewal";
            case REJECTED -> "No further workflow";
            case RENEWAL_DUE -> "Review renewal";
        };
    }
}
