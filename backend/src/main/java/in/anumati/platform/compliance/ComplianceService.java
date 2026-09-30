package in.anumati.platform.compliance;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.regulatory.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ComplianceService {
    private final ComplianceRepository repo;
    private final BusinessProfileService profiles;
    private final ApplicationRepository apps;
    private final ApprovalRepository approvals;
    private final RegulatorySourceRepository sources;
    private final AuditService audit;

    public ComplianceService(ComplianceRepository r, BusinessProfileService p, ApplicationRepository a, ApprovalRepository ap, RegulatorySourceRepository s, AuditService x) {
        repo=r; profiles=p; apps=a; approvals=ap; sources=s; audit=x;
    }

    @Transactional
    public ComplianceResponse create(ComplianceRequest req, String actor) {
        BusinessProfile profile = profiles.get(req.businessProfileId());
        RegulatorySource source = sources.findById(req.sourceId()).orElseThrow(() -> new IllegalArgumentException("Source not found"));
        if (source.getVerificationStatus() != SourceVerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("Compliance source must be verified");
        }

        ApplicationRecord app = req.applicationId() == null ? null : apps.findById(req.applicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        Approval approval = req.approvalId() == null ? null : approvals.findById(req.approvalId())
                .orElseThrow(() -> new IllegalArgumentException("Approval not found"));

        if (app != null && !app.getBusinessProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Application does not belong to the requested business profile");
        }
        if (approval != null && app != null && !app.getApproval().getId().equals(approval.getId())) {
            throw new IllegalArgumentException("Approval does not match the linked application");
        }

        var c = repo.save(new ComplianceObligation(profile, app, approval, req.name(), req.authority(), req.description(), req.dueDate(), req.reminderDays(), req.frequency(), source));
        audit.record(actor, "COMPLIANCE_CREATED", "ComplianceObligation", c.getId(), Map.of("businessProfileId", profile.getId()));
        return ComplianceResponse.from(c);
    }

    @Transactional(readOnly = true)
    public List<ComplianceResponse> business(UUID profileId, String actor) {
        profiles.getForActor(profileId, actor);
        return repo.findByBusinessProfile_IdOrderByDueDateAsc(profileId).stream().peek(ComplianceObligation::refresh).map(ComplianceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ComplianceResponse> all() {
        return repo.findAllByOrderByDueDateAsc().stream().peek(ComplianceObligation::refresh).map(ComplianceResponse::from).toList();
    }

    @Transactional
    public ComplianceResponse complete(UUID id, String actor, boolean dept) {
        var c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Compliance obligation not found"));
        if (!dept && !c.getBusinessProfile().getOwnerActor().equals(actor)) {
            throw new AccessDeniedException("Not permitted");
        }
        c.complete();
        audit.record(actor, "COMPLIANCE_COMPLETED", "ComplianceObligation", id, Map.of());
        if (c.isRecurring()) {
            java.time.LocalDate nextDue = c.nextDueDate();
            if (!repo.existsByBusinessProfile_IdAndNameAndDueDate(c.getBusinessProfile().getId(), c.getName(), nextDue)) {
                ComplianceObligation next = repo.save(new ComplianceObligation(
                        c.getBusinessProfile(), c.getApplication(), c.getApproval(), c.getName(), c.getAuthority(),
                        c.getDescription(), nextDue, c.getReminderDays(), c.getFrequency(), c.getSource()));
                audit.record(actor, "COMPLIANCE_NEXT_CYCLE_CREATED", "ComplianceObligation", next.getId(), Map.of("previousObligationId", id.toString(), "dueDate", nextDue.toString()));
            }
        }
        return ComplianceResponse.from(c);
    }
}
