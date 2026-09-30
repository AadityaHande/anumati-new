package in.anumati.platform.application;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class ApplicationStateMachine {
    public boolean isAllowed(ApplicationStatus from, ApplicationStatus to) {
        return allowed(from).contains(to);
    }

    public Set<ApplicationStatus> allowed(ApplicationStatus current) {
        return switch (current) {
            case SUBMITTED -> EnumSet.of(ApplicationStatus.UNDER_SCRUTINY);
            case UNDER_SCRUTINY -> EnumSet.of(ApplicationStatus.QUERY_RAISED, ApplicationStatus.INSPECTION_SCHEDULED, ApplicationStatus.DECISION);
            case QUERY_RAISED -> EnumSet.of(ApplicationStatus.RESUBMITTED);
            case RESUBMITTED -> EnumSet.of(ApplicationStatus.UNDER_SCRUTINY, ApplicationStatus.INSPECTION_SCHEDULED);
            case INSPECTION_SCHEDULED -> EnumSet.of(ApplicationStatus.INSPECTION_COMPLETE);
            case INSPECTION_COMPLETE -> EnumSet.of(ApplicationStatus.DECISION);
            case DECISION -> EnumSet.of(ApplicationStatus.APPROVED, ApplicationStatus.REJECTED);
            case APPROVED -> EnumSet.of(ApplicationStatus.RENEWAL_DUE);
            case REJECTED, RENEWAL_DUE -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }
}
