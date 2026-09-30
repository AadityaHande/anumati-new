package in.anumati.platform.operations;

import in.anumati.platform.analysis.AnalysisRunRepository;
import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.application.ApplicationStatus;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.dependency.ApprovalDependency;
import in.anumati.platform.dependency.ApprovalDependencyRepository;
import in.anumati.platform.dependency.DependencyType;
import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.sla.SlaResponse;
import in.anumati.platform.sla.SlaService;
import in.anumati.platform.sla.SlaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ControlTowerServiceTest {

    @Mock ApplicationRepository applications;
    @Mock ApprovalDependencyRepository dependencies;
    @Mock SlaService slaService;
    @Mock AnalysisRunRepository analyses;

    @Test
    void summaryBuildsHealthyLane() {
        ApplicationRecord application = application(ApplicationStatus.UNDER_SCRUTINY, "APP-001", "Food Unit", "Pune");
        when(applications.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(application));
        when(dependencies.findByActiveTrue()).thenReturn(List.of());
        when(analyses.findAllById(any())).thenReturn(List.of());
        UUID applicationId = application.getId();
        Instant submittedAt = application.getSubmittedAt();
        doReturn(Map.of(applicationId,
                new SlaResponse(applicationId, SlaStatus.ON_TRACK, submittedAt,
                        submittedAt.plusSeconds(3600), 3600, 24, 6))).when(slaService).statuses(List.of(application));

        ControlTowerResponse response = service().summary();

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.blocked()).isZero();
        assertThat(response.atRisk()).isZero();
        assertThat(response.overdue()).isZero();
        assertThat(response.lanes()).singleElement().satisfies(lane -> {
            assertThat(lane.status()).isEqualTo(ApplicationStatus.UNDER_SCRUTINY.name());
            assertThat(lane.slaStatus()).isEqualTo(SlaStatus.ON_TRACK.name());
            assertThat(lane.nextAction()).contains("Review evidence");
        });
    }

    @Test
    void summaryMarksUnapprovedBlockingDependency() {
        ApplicationRecord application = application(ApplicationStatus.SUBMITTED, "APP-002", "Food Unit", "Pune");
        Approval currentApproval = application.getApproval();
        Approval prerequisite = dependencyApproval("FIRE-NOC");
        ApprovalDependency dependency = new ApprovalDependency(currentApproval, prerequisite, DependencyType.BLOCKING,
                "Prerequisite", true);

        BusinessProfile currentProfile = application.getBusinessProfile();
        when(currentProfile.getId()).thenReturn(UUID.randomUUID());
        when(applications.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(application));
        when(dependencies.findByActiveTrue()).thenReturn(List.of(dependency));
        when(analyses.findAllById(any())).thenReturn(List.of());
        UUID applicationId = application.getId();
        Instant submittedAt = application.getSubmittedAt();
        doReturn(Map.of(applicationId,
                new SlaResponse(applicationId, SlaStatus.ON_TRACK, submittedAt,
                        submittedAt.plusSeconds(3600), 3600, 24, 6))).when(slaService).statuses(List.of(application));

        ControlTowerResponse response = service().summary();

        assertThat(response.blocked()).isEqualTo(1);
        assertThat(response.lanes()).singleElement().satisfies(lane -> {
            assertThat(lane.blockers()).containsExactly("FIRE-NOC");
            assertThat(lane.nextAction()).isEqualTo("Resolve blocking approval dependency");
        });
    }

    @Test
    void summaryShowsOverdueLaneAndEscalationAction() {
        ApplicationRecord application = application(ApplicationStatus.UNDER_SCRUTINY, "APP-003", "Food Unit", "Pune");
        when(applications.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(application));
        when(dependencies.findByActiveTrue()).thenReturn(List.of());
        when(analyses.findAllById(any())).thenReturn(List.of());
        UUID applicationId = application.getId();
        Instant submittedAt = application.getSubmittedAt();
        doReturn(Map.of(applicationId,
                new SlaResponse(applicationId, SlaStatus.OVERDUE, submittedAt,
                        submittedAt.minusSeconds(60), -60, 24, 6))).when(slaService).statuses(List.of(application));

        ControlTowerResponse response = service().summary();

        assertThat(response.overdue()).isEqualTo(1);
        assertThat(response.lanes()).singleElement().satisfies(lane -> {
            assertThat(lane.slaStatus()).isEqualTo(SlaStatus.OVERDUE.name());
            assertThat(lane.nextAction()).isEqualTo("Escalate service timeline");
        });
    }

    @Test
    void summaryDoesNotBlockWhenDependencyApprovalIsAlreadyApproved() {
        ApplicationRecord current = application(ApplicationStatus.SUBMITTED, "APP-004", "Food Unit", "Pune");
        BusinessProfile currentProfile = current.getBusinessProfile();
        when(currentProfile.getId()).thenReturn(UUID.randomUUID());
        Approval prerequisite = dependencyApproval("FIRE-NOC");
        ApplicationRecord prerequisiteApplication = approvedApplication(currentProfile, prerequisite, "APP-005");
        ApprovalDependency dependency = new ApprovalDependency(current.getApproval(), prerequisite, DependencyType.BLOCKING,
                "Prerequisite", true);
        when(applications.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(current, prerequisiteApplication));
        when(dependencies.findByActiveTrue()).thenReturn(List.of(dependency));
        when(analyses.findAllById(any())).thenReturn(List.of());
        UUID currentId = current.getId();
        Instant currentSubmittedAt = current.getSubmittedAt();
        UUID prerequisiteId = prerequisiteApplication.getId();
        Instant prerequisiteSubmittedAt = prerequisiteApplication.getSubmittedAt();
        doReturn(Map.of(
                currentId, new SlaResponse(currentId, SlaStatus.ON_TRACK, currentSubmittedAt,
                        currentSubmittedAt.plusSeconds(3600), 3600, 24, 6),
                prerequisiteId, new SlaResponse(prerequisiteId, SlaStatus.COMPLETE,
                        prerequisiteSubmittedAt, prerequisiteSubmittedAt, 0, 24, 6))).when(slaService).statuses(List.of(current, prerequisiteApplication));

        ControlTowerResponse response = service().summary();

        assertThat(response.blocked()).isZero();
        assertThat(response.lanes()).first().satisfies(lane -> assertThat(lane.blockers()).isEmpty());
    }

    private ControlTowerService service() {
        return new ControlTowerService(applications, dependencies, slaService, analyses);
    }

    private ApplicationRecord application(ApplicationStatus status, String reference, String businessName, String district) {
        BusinessProfile profile = org.mockito.Mockito.mock(BusinessProfile.class);
        when(profile.getBusinessName()).thenReturn(businessName);
        when(profile.getDistrict()).thenReturn(district);

        Approval approval = approval("APPROVAL-001", "Primary Approval");
        ApplicationRecord app = org.mockito.Mockito.mock(ApplicationRecord.class);
        UUID id = UUID.randomUUID();
        when(app.getId()).thenReturn(id);
        when(app.getBusinessProfile()).thenReturn(profile);
        when(app.getApproval()).thenReturn(approval);
        when(app.getAnalysisRunId()).thenReturn(UUID.randomUUID());
        String approvalCode = approval.getCode();
        String approvalName = approval.getName();
        String approvalAuthority = approval.getAuthority();
        when(app.getApprovalCodeSnapshot()).thenReturn(approvalCode);
        when(app.getApprovalNameSnapshot()).thenReturn(approvalName);
        when(app.getAuthoritySnapshot()).thenReturn(approvalAuthority);
        when(app.getExternalReference()).thenReturn(reference);
        when(app.getStatus()).thenReturn(status);
        when(app.getSubmittedAt()).thenReturn(Instant.now());
        when(app.getUpdatedAt()).thenReturn(Instant.now());
        return app;
    }


    private ApplicationRecord approvedApplication(BusinessProfile profile, Approval approval, String reference) {
        ApplicationRecord app = org.mockito.Mockito.mock(ApplicationRecord.class);
        UUID id = UUID.randomUUID();
        Instant submittedAt = Instant.now();
        when(app.getId()).thenReturn(id);
        when(app.getBusinessProfile()).thenReturn(profile);
        when(app.getApproval()).thenReturn(approval);
        when(app.getAnalysisRunId()).thenReturn(UUID.randomUUID());
        String approvalCode = approval.getCode();
        when(app.getApprovalCodeSnapshot()).thenReturn(approvalCode);
        when(app.getApprovalNameSnapshot()).thenReturn("Fire NOC");
        when(app.getAuthoritySnapshot()).thenReturn("Department");
        when(app.getExternalReference()).thenReturn(reference);
        when(app.getStatus()).thenReturn(ApplicationStatus.APPROVED);
        when(app.getSubmittedAt()).thenReturn(submittedAt);
        when(app.getUpdatedAt()).thenReturn(submittedAt);
        return app;
    }

    private Approval approval(String code, String name) {
        Approval approval = org.mockito.Mockito.mock(Approval.class);
        when(approval.getId()).thenReturn(UUID.randomUUID());
        when(approval.getCode()).thenReturn(code);
        when(approval.getName()).thenReturn(name);
        when(approval.getAuthority()).thenReturn("Department");
        return approval;
    }

    private Approval dependencyApproval(String code) {
        Approval approval = org.mockito.Mockito.mock(Approval.class);
        when(approval.getId()).thenReturn(UUID.randomUUID());
        when(approval.getCode()).thenReturn(code);
        return approval;
    }
}
