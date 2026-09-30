package in.anumati.platform;

import in.anumati.platform.application.ApplicationStateMachine;
import in.anumati.platform.application.ApplicationStatus;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationStateMachineTest {
    private final ApplicationStateMachine machine = new ApplicationStateMachine();

    @Test
    void rejectsSkippingScrutiny() {
        assertFalse(machine.isAllowed(ApplicationStatus.SUBMITTED, ApplicationStatus.DECISION));
    }

    @Test
    void allowsQueryAndResponseLoop() {
        assertTrue(machine.isAllowed(ApplicationStatus.UNDER_SCRUTINY, ApplicationStatus.QUERY_RAISED));
        assertTrue(machine.isAllowed(ApplicationStatus.QUERY_RAISED, ApplicationStatus.RESUBMITTED));
        assertTrue(machine.isAllowed(ApplicationStatus.RESUBMITTED, ApplicationStatus.UNDER_SCRUTINY));
    }

    @Test
    void requiresInspectionToCompleteBeforeDecisionWhenThatPathIsUsed() {
        assertTrue(machine.isAllowed(ApplicationStatus.INSPECTION_SCHEDULED, ApplicationStatus.INSPECTION_COMPLETE));
        assertTrue(machine.isAllowed(ApplicationStatus.INSPECTION_COMPLETE, ApplicationStatus.DECISION));
        assertFalse(machine.isAllowed(ApplicationStatus.INSPECTION_SCHEDULED, ApplicationStatus.DECISION));
    }

    @Test
    void documentsExpectedLifecycleStates() {
        assertAll(
                () -> assertEquals(10, EnumSet.allOf(ApplicationStatus.class).size()),
                () -> assertNotNull(ApplicationStatus.RENEWAL_DUE)
        );
    }
}
