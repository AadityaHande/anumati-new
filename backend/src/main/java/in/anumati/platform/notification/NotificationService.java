package in.anumati.platform.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;
import java.util.UUID;

import in.anumati.platform.application.events.ApplicationQueryRaisedEvent;
import in.anumati.platform.application.events.ApplicationQueryRespondedEvent;
import in.anumati.platform.application.events.InspectionScheduledEvent;
import in.anumati.platform.application.events.ApplicationDecisionRecordedEvent;
import in.anumati.platform.grievance.events.GrievanceCreatedEvent;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) { this.repository = repository; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationQueryRaised(ApplicationQueryRaisedEvent event) {
        create(event.recipientActor(), "QUERY", "Action needed on your application", event.subject(), "Application", event.applicationId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationQueryResponded(ApplicationQueryRespondedEvent event) {
        create(event.recipientActor(), "QUERY_RESPONSE", "Applicant responded to a query", "A response was received for your query.", "ApplicationQuery", event.queryId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInspectionScheduled(InspectionScheduledEvent event) {
        create(event.recipientActor(), "INSPECTION", "Inspection scheduled", "An inspection has been scheduled.", "Inspection", event.inspectionId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDecisionRecorded(ApplicationDecisionRecordedEvent event) {
        create(event.recipientActor(), "DECISION", "Application decision recorded", event.outcome().name(), "Application", event.applicationId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGrievanceCreated(GrievanceCreatedEvent event) {
        create(event.recipientActor(), "GRIEVANCE", "Grievance received", "Your grievance has been recorded.", "Grievance", event.grievanceId());
    }

    @Transactional
    public void create(String recipient, String type, String title, String body, String entityType, UUID entityId) {
        if (entityId != null && repository.existsByRecipientAndTypeAndEntityId(recipient, type, entityId)) return;
        repository.save(new Notification(recipient, type, title, body, entityType, entityId));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String recipient) {
        return repository.findByRecipientOrderByCreatedAtDesc(recipient).stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long unread(String recipient) {
        return repository.countByRecipientAndReadAtIsNull(recipient);
    }

    @Transactional
    public void read(UUID id, String actor) {
        Notification notification = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getRecipient().equals(actor)) {
            throw new org.springframework.security.access.AccessDeniedException("Not permitted");
        }
        notification.markRead();
    }
}
