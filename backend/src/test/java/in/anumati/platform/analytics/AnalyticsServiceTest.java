package in.anumati.platform.analytics;

import in.anumati.platform.application.*;
import in.anumati.platform.compliance.ComplianceObligation;
import in.anumati.platform.compliance.ComplianceRepository;
import in.anumati.platform.compliance.ComplianceStatus;
import in.anumati.platform.grievance.GrievanceRepository;
import in.anumati.platform.grievance.GrievanceStatus;
import in.anumati.platform.sla.SlaResponse;
import in.anumati.platform.sla.SlaService;
import in.anumati.platform.sla.SlaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock ApplicationRepository applications;
    @Mock ApplicationQueryRepository queries;

    @Mock GrievanceRepository grievances;
    @Mock ComplianceRepository compliance;
    @Mock SlaService slaService;

    @Test
    void summaryAggregatesCoreOperationalCounts() {
        ApplicationRecord pending = application(ApplicationStatus.UNDER_SCRUTINY, "MPCB");
        ApplicationRecord approved = application(ApplicationStatus.APPROVED, "Fire");
        when(applications.findAll()).thenReturn(List.of(pending, approved));
        UUID pendingId = pending.getId();
        UUID approvedId = approved.getId();
        doReturn(List.of(query(QueryStatus.OPEN))).when(queries).findByApplication_IdOrderByRaisedAtAsc(pendingId);
        doReturn(List.of(query(QueryStatus.CLOSED))).when(queries).findByApplication_IdOrderByRaisedAtAsc(approvedId);
        doReturn(sla(pending, SlaStatus.ON_TRACK)).when(slaService).status(pending);
        doReturn(sla(approved, SlaStatus.COMPLETE)).when(slaService).status(approved);
        when(grievances.countByStatus(GrievanceStatus.OPEN)).thenReturn(2L);
        ComplianceObligation due = mock(ComplianceObligation.class);
        when(due.getStatus()).thenReturn(ComplianceStatus.DUE);
        when(compliance.findAllByOrderByDueDateAsc()).thenReturn(List.of(due));

        DepartmentDashboardResponse response = service().summary();

        assertThat(response.totalApplications()).isEqualTo(2);
        assertThat(response.pendingScrutiny()).isEqualTo(1);
        assertThat(response.queriesAwaitingApplicant()).isEqualTo(1);
        assertThat(response.approved()).isEqualTo(1);
        assertThat(response.openGrievances()).isEqualTo(2);
        assertThat(response.complianceDue()).isEqualTo(1);
        assertThat(response.byDepartment()).containsEntry("MPCB", 1L).containsEntry("Fire", 1L);
    }

    @Test
    void summaryCountsSlaOverdueApplications() {
        ApplicationRecord app = application(ApplicationStatus.UNDER_SCRUTINY, "MPCB");
        when(applications.findAll()).thenReturn(List.of(app));
        UUID appId = app.getId();
        doReturn(List.of()).when(queries).findByApplication_IdOrderByRaisedAtAsc(appId);
        doReturn(sla(app, SlaStatus.OVERDUE)).when(slaService).status(app);
        when(grievances.countByStatus(GrievanceStatus.OPEN)).thenReturn(0L);
        when(compliance.findAllByOrderByDueDateAsc()).thenReturn(List.of());

        DepartmentDashboardResponse response = service().summary();

        assertThat(response.slaAtRisk()).isZero();
        assertThat(response.overdue()).isEqualTo(1);
    }

    @Test
    void summaryCountsInspectionAndRejectStates() {
        ApplicationRecord inspection = application(ApplicationStatus.INSPECTION_SCHEDULED, "Fire");
        ApplicationRecord rejected = application(ApplicationStatus.REJECTED, "Labour");
        when(applications.findAll()).thenReturn(List.of(inspection, rejected));
        when(queries.findByApplication_IdOrderByRaisedAtAsc(any())).thenReturn(List.of());
        doReturn(sla(inspection, SlaStatus.ON_TRACK)).when(slaService).status(inspection);
        doReturn(sla(rejected, SlaStatus.COMPLETE)).when(slaService).status(rejected);
        when(grievances.countByStatus(GrievanceStatus.OPEN)).thenReturn(0L);
        when(compliance.findAllByOrderByDueDateAsc()).thenReturn(List.of());

        DepartmentDashboardResponse response = service().summary();

        assertThat(response.inspectionsPending()).isEqualTo(1);
        assertThat(response.rejected()).isEqualTo(1);
        assertThat(response.byStatus()).containsEntry(ApplicationStatus.INSPECTION_SCHEDULED.name(), 1L)
                .containsEntry(ApplicationStatus.REJECTED.name(), 1L);
    }

    @Test
    void summaryCountsAtRiskApplicationsSeparatelyFromOverdue() {
        ApplicationRecord app = application(ApplicationStatus.SUBMITTED, "Food");
        when(applications.findAll()).thenReturn(List.of(app));
        UUID appId = app.getId();
        doReturn(List.of()).when(queries).findByApplication_IdOrderByRaisedAtAsc(appId);
        doReturn(sla(app, SlaStatus.AT_RISK)).when(slaService).status(app);
        when(grievances.countByStatus(GrievanceStatus.OPEN)).thenReturn(0L);
        when(compliance.findAllByOrderByDueDateAsc()).thenReturn(List.of());

        DepartmentDashboardResponse response = service().summary();

        assertThat(response.slaAtRisk()).isEqualTo(1);
        assertThat(response.overdue()).isZero();
    }

    private AnalyticsService service() {
        return new AnalyticsService(applications, queries, grievances, compliance, slaService);
    }

    private ApplicationRecord application(ApplicationStatus status, String authority) {
        ApplicationRecord app = mock(ApplicationRecord.class);
        when(app.getId()).thenReturn(UUID.randomUUID());
        when(app.getStatus()).thenReturn(status);
        when(app.getAuthoritySnapshot()).thenReturn(authority);
        return app;
    }

    private ApplicationQuery query(QueryStatus status) {
        ApplicationQuery query = mock(ApplicationQuery.class);
        when(query.getStatus()).thenReturn(status);
        return query;
    }

    private SlaResponse sla(ApplicationRecord app, SlaStatus status) {
        return new SlaResponse(app.getId(), status, Instant.now(), Instant.now().plusSeconds(3600),
                3600, 24, 6);
    }
}
