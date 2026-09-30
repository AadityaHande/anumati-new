package in.anumati.platform;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.renewal.RenewalRecord;
import in.anumati.platform.renewal.RenewalStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RenewalRecordTest {
    @Test
    void completedRenewalDoesNotRecomputeToExpired() {
        ApplicationRecord application = mock(ApplicationRecord.class);
        when(application.getBusinessProfile()).thenReturn(mock(BusinessProfile.class));
        when(application.getApproval()).thenReturn(mock(Approval.class));

        RenewalRecord renewal = new RenewalRecord(application, LocalDate.now().minusDays(30), LocalDate.now().minusDays(1), 30, null);
        renewal.markCompleted();
        renewal.refresh();

        assertThat(renewal.getStatus()).isEqualTo(RenewalStatus.COMPLETED);
    }

    @Test
    void inProgressRenewalDoesNotRecomputeToExpired() {
        ApplicationRecord application = mock(ApplicationRecord.class);
        when(application.getBusinessProfile()).thenReturn(mock(BusinessProfile.class));
        when(application.getApproval()).thenReturn(mock(Approval.class));

        RenewalRecord renewal = new RenewalRecord(application, LocalDate.now().minusDays(30), LocalDate.now().minusDays(1), 30, null);
        renewal.markInProgress();
        renewal.refresh();

        assertThat(renewal.getStatus()).isEqualTo(RenewalStatus.IN_PROGRESS);
    }
}
