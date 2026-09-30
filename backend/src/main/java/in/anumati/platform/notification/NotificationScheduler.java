package in.anumati.platform.notification;

import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.application.ApplicationStatus;
import in.anumati.platform.compliance.ComplianceObligation;
import in.anumati.platform.compliance.ComplianceRepository;
import in.anumati.platform.renewal.RenewalRecord;
import in.anumati.platform.renewal.RenewalRepository;
import in.anumati.platform.renewal.RenewalStatus;
import in.anumati.platform.sla.SlaResponse;
import in.anumati.platform.sla.SlaService;
import in.anumati.platform.sla.SlaStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;

@Component
public class NotificationScheduler {
    private final ApplicationRepository applications;
    private final SlaService slaService;
    private final RenewalRepository renewals;
    private final ComplianceRepository compliance;
    private final NotificationService notifications;
    private final boolean enabled;

    public NotificationScheduler(
            ApplicationRepository applications,
            SlaService slaService,
            RenewalRepository renewals,
            ComplianceRepository compliance,
            NotificationService notifications,
            @Value("${anumati.jobs.notifications.enabled:true}") boolean enabled) {
        this.applications = applications;
        this.slaService = slaService;
        this.renewals = renewals;
        this.compliance = compliance;
        this.notifications = notifications;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${anumati.jobs.notifications.interval-ms:900000}", initialDelayString = "${anumati.jobs.notifications.initial-delay-ms:30000}")
    @Transactional
    public void publishOperationalAttention() {
        if (!enabled) return;

        for (var application : applications.findByStatusIn(EnumSet.of(
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.UNDER_SCRUTINY,
                ApplicationStatus.QUERY_RAISED,
                ApplicationStatus.RESUBMITTED,
                ApplicationStatus.INSPECTION_SCHEDULED,
                ApplicationStatus.INSPECTION_COMPLETE))) {
            SlaResponse status = slaService.status(application);
            if (status.status() == SlaStatus.AT_RISK) {
                notifications.create(application.getBusinessProfile().getOwnerActor(), "SLA_AT_RISK",
                        "Application timeline needs attention", "The service timeline is approaching its warning threshold.", "Application", application.getId());
            } else if (status.status() == SlaStatus.OVERDUE) {
                notifications.create(application.getBusinessProfile().getOwnerActor(), "SLA_OVERDUE",
                        "Application timeline is overdue", "The configured service timeline has passed.", "Application", application.getId());
            }
        }

        LocalDate today = LocalDate.now();
        for (RenewalRecord renewal : renewals.findByStatusIn(EnumSet.of(RenewalStatus.UPCOMING, RenewalStatus.DUE, RenewalStatus.EXPIRED))) {
            renewal.refresh();
            if (renewal.getStatus() == RenewalStatus.DUE || renewal.getStatus() == RenewalStatus.EXPIRED) {
                notifications.create(renewal.getBusinessProfile().getOwnerActor(), "RENEWAL_DUE",
                        renewal.getStatus() == RenewalStatus.EXPIRED ? "Renewal is overdue" : "Renewal is due",
                        "Approval renewal date: " + renewal.getValidUntil(), "Renewal", renewal.getId());
            }
        }

        for (ComplianceObligation obligation : compliance.findAllByOrderByDueDateAsc()) {
            obligation.refresh();
            if ((obligation.getStatus().name().equals("DUE") || obligation.getStatus().name().equals("OVERDUE"))
                    && !obligation.getDueDate().isAfter(today)) {
                notifications.create(obligation.getBusinessProfile().getOwnerActor(), "COMPLIANCE_DUE",
                        obligation.getStatus().name().equals("OVERDUE") ? "Compliance obligation is overdue" : "Compliance obligation is due",
                        obligation.getName() + " is due on " + obligation.getDueDate(), "Compliance", obligation.getId());
            }
        }
    }
}
