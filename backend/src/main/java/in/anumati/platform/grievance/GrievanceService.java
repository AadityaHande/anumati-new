package in.anumati.platform.grievance;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.grievance.events.GrievanceCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GrievanceService {
    private final GrievanceRepository repository;
    private final BusinessProfileService profiles;
    private final ApplicationRepository applications;
    private final AuditService audit;
    private final ApplicationEventPublisher events;

    public GrievanceService(GrievanceRepository repository, BusinessProfileService profiles, ApplicationRepository applications, AuditService audit, ApplicationEventPublisher events) {
        this.repository = repository;
        this.profiles = profiles;
        this.applications = applications;
        this.audit = audit;
        this.events = events;
    }

    @Transactional
    public GrievanceResponse create(GrievanceRequest request, String actor) {
        BusinessProfile profile = profiles.getForActor(request.businessProfileId(), actor);
        ApplicationRecord application = null;
        if (request.applicationId() != null) {
            application = applications.findById(request.applicationId()).orElseThrow(() -> new IllegalArgumentException("Application not found"));
            if (!application.getBusinessProfile().getId().equals(profile.getId())) {
                throw new IllegalArgumentException("Application does not belong to the business profile");
            }
        }
        Grievance grievance = repository.save(new Grievance(profile, application, request.subject(), request.description(), request.department(), request.priority()));
        audit.record(actor, "GRIEVANCE_CREATED", "Grievance", grievance.getId(), Map.of("businessProfileId", profile.getId()));
        events.publishEvent(new GrievanceCreatedEvent(grievance.getId(), profile.getId(), profile.getOwnerActor(), actor));
        return GrievanceResponse.from(grievance);
    }

    @Transactional(readOnly = true)
    public List<GrievanceResponse> business(UUID id, String actor) {
        profiles.getForActor(id, actor);
        return repository.findByBusinessProfile_IdOrderByCreatedAtDesc(id).stream().map(GrievanceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<GrievanceResponse> all() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(GrievanceResponse::from).toList();
    }

    @Transactional
    public GrievanceResponse update(UUID id, GrievanceUpdateRequest request, String actor) {
        Grievance grievance = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Grievance not found"));
        if (request.assignedTo() != null && !request.assignedTo().isBlank()) grievance.assign(request.assignedTo());
        if (request.status() == GrievanceStatus.IN_PROGRESS) grievance.progress();
        if (request.status() == GrievanceStatus.RESOLVED) {
            if (request.resolution() == null || request.resolution().isBlank()) throw new IllegalArgumentException("A resolved grievance requires a resolution");
            grievance.resolve(request.resolution());
        }
        if (request.status() == GrievanceStatus.CLOSED) {
            if (grievance.getStatus() != GrievanceStatus.RESOLVED) throw new IllegalArgumentException("Only a resolved grievance can be closed");
            grievance.close();
        }
        audit.record(actor, "GRIEVANCE_UPDATED", "Grievance", id, Map.of("status", grievance.getStatus().name()));
        return GrievanceResponse.from(grievance);
    }
}
