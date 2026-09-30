package in.anumati.platform.sla;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.application.ApplicationStatus;
import in.anumati.platform.application.events.ApplicationSubmittedEvent;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.regulatory.ApprovalRepository;
import in.anumati.platform.regulatory.RegulatorySource;
import in.anumati.platform.regulatory.RegulatorySourceRepository;
import in.anumati.platform.regulatory.SourceVerificationStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SlaService {
    private final ApprovalSlaConfigRepository configs;
    private final ApplicationRepository applications;
    private final ApprovalRepository approvals;
    private final RegulatorySourceRepository sources;
    private final AuditService audit;

    public SlaService(ApprovalSlaConfigRepository configs, ApplicationRepository applications,
                      ApprovalRepository approvals, RegulatorySourceRepository sources, AuditService audit) {
        this.configs = configs;
        this.applications = applications;
        this.approvals = approvals;
        this.sources = sources;
        this.audit = audit;
    }

    @EventListener
    @Transactional
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        ApplicationRecord application = applications.findById(event.applicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + event.applicationId()));
        attachDueDate(application);
    }

    @Transactional
    public void attachDueDate(ApplicationRecord application) {
        configs.findByApproval_Id(application.getApproval().getId())
                .filter(ApprovalSlaConfig::isActive)
                .filter(config -> matchesScope(config, application))
                .ifPresent(config -> {
                    ensureVerifiedSource(config.getSource());
                    application.setSlaDueAt(application.getSubmittedAt().plus(Duration.ofHours(config.getTargetHours())));
                    applications.save(application);
                });
    }

    @Transactional(readOnly = true)
    public SlaResponse status(ApplicationRecord application) {
        Optional<ApprovalSlaConfig> existing = configs.findByApproval_Id(application.getApproval().getId());
        ApprovalSlaConfig config = existing.filter(c -> c.isActive() && matchesScope(c, application)).orElse(null);
        return statusWithConfig(application, config);
    }

    @Transactional(readOnly = true)
    public java.util.Map<UUID, SlaResponse> statuses(List<ApplicationRecord> applicationsList) {
        if (applicationsList.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.Set<UUID> approvalIds = applicationsList.stream()
                .map(application -> application.getApproval().getId())
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<UUID, ApprovalSlaConfig> configByApproval = configs.findByApproval_IdIn(approvalIds).stream()
                .collect(java.util.stream.Collectors.toMap(c -> c.getApproval().getId(), java.util.function.Function.identity(), (a, b) -> a));

        java.util.Map<UUID, SlaResponse> result = new java.util.HashMap<>();
        for (ApplicationRecord application : applicationsList) {
            ApprovalSlaConfig config = configByApproval.get(application.getApproval().getId());
            if (config != null && (!config.isActive() || !matchesScope(config, application))) config = null;
            result.put(application.getId(), statusWithConfig(application, config));
        }
        return result;
    }

    private SlaResponse statusWithConfig(ApplicationRecord application, ApprovalSlaConfig config) {
        if (config == null || !config.isActive()) {
            return new SlaResponse(application.getId(), SlaStatus.NOT_CONFIGURED, application.getSubmittedAt(), null, 0, null, null);
        }
        if (application.getStatus() == ApplicationStatus.APPROVED
                || application.getStatus() == ApplicationStatus.REJECTED
                || application.getStatus() == ApplicationStatus.RENEWAL_DUE) {
            return new SlaResponse(application.getId(), SlaStatus.COMPLETE, application.getSubmittedAt(),
                    application.getSlaDueAt(), 0, config.getTargetHours(), config.getWarningHours());
        }
        Instant due = application.getSlaDueAt() != null
                ? application.getSlaDueAt()
                : application.getSubmittedAt().plus(Duration.ofHours(config.getTargetHours()));
        long remainingSeconds = Duration.between(Instant.now(), due).getSeconds();
        long warningSeconds = Duration.ofHours(config.getWarningHours()).getSeconds();
        SlaStatus status = remainingSeconds < 0
                ? SlaStatus.OVERDUE
                : remainingSeconds <= warningSeconds ? SlaStatus.AT_RISK : SlaStatus.ON_TRACK;
        return new SlaResponse(application.getId(), status, application.getSubmittedAt(), due, remainingSeconds,
                config.getTargetHours(), config.getWarningHours());
    }

    @Transactional
    public ApprovalSlaConfigResponse upsert(ApprovalSlaConfigRequest request, String actor) {
        UUID approvalId = request.approvalId();
        int targetHours = request.targetHours();
        int warningHours = request.warningHours();
        UUID sourceId = request.sourceId();
        boolean active = Boolean.TRUE.equals(request.active());
        Approval approval = approvals.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + approvalId));
        RegulatorySource source = sources.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));
        ensureVerifiedSource(source);
        if (warningHours < 0 || warningHours >= targetHours) {
            throw new IllegalArgumentException("warningHours must be >= 0 and less than targetHours");
        }

        ApprovalSlaConfig config = configs.findByApproval_Id(approvalId).orElse(null);
        String action;
        if (config == null) {
            config = configs.save(new ApprovalSlaConfig(approval, targetHours, warningHours, source, active, request.scopeAttributes(), request.maxInvestmentInr()));
            action = "SLA_CONFIG_CREATED";
        } else {
            config.update(targetHours, warningHours, source, active, request.scopeAttributes(), request.maxInvestmentInr());
            config = configs.save(config);
            action = "SLA_CONFIG_UPDATED";
        }
        audit.record(actor, action, "ApprovalSlaConfig", config.getId(), java.util.Map.of("approvalId", approvalId));
        return ApprovalSlaConfigResponse.from(config);
    }

    @Transactional(readOnly = true)
    public List<ApprovalSlaConfigResponse> list() {
        return configs.findByActiveTrue().stream().map(ApprovalSlaConfigResponse::from).toList();
    }

    private boolean matchesScope(ApprovalSlaConfig config, ApplicationRecord application) {
        Map<String, String> scope = config.getScopeAttributes();
        if (scope.isEmpty()) return true;
        Map<String, String> attributes = application.getBusinessProfile().getRegulatoryAttributes();
        boolean attributesMatch = scope.entrySet().stream().allMatch(entry -> {
            String actual = attributes.get(entry.getKey());
            return actual != null && actual.equalsIgnoreCase(entry.getValue());
        });
        if (!attributesMatch) return false;
        BigDecimal maxInvestment = config.getMaxInvestmentInr();
        return maxInvestment == null || application.getBusinessProfile().getInvestmentInr().compareTo(maxInvestment) <= 0;
    }

    private void ensureVerifiedSource(RegulatorySource source) {
        if (source == null || source.getVerificationStatus() != SourceVerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("SLA source must be verified");
        }
    }
}
