package in.anumati.platform.operations;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.Inspection;
import in.anumati.platform.application.InspectionOutcome;
import in.anumati.platform.application.InspectionRepository;
import in.anumati.platform.business.BusinessProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectionPlannerServiceTest {

    @Mock InspectionRepository inspections;

    @Test
    void plannerReturnsFutureInspectionsAndMapsBusinessContext() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        Inspection inspection = inspection(future, "MPCB", "Officer A");
        when(inspections.findAllByOrderByScheduledAtAsc()).thenReturn(List.of(inspection));

        InspectionPlannerResponse response = service().plan();

        assertThat(response.inspections()).hasSize(1);
        assertThat(response.inspections().getFirst().authority()).isEqualTo("MPCB");
        assertThat(response.inspections().getFirst().assignedOfficer()).isEqualTo("Officer A");
        assertThat(response.coordinationOpportunities()).isEmpty();
    }

    @Test
    void plannerFindsCrossDepartmentCoordinationOpportunityWithinWindow() {
        UUID businessId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Inspection a = inspectionForBusiness(businessId, start, "MPCB", "Officer A");
        Inspection b = inspectionForBusiness(businessId, start.plusDays(2), "Fire", "Officer B");
        when(inspections.findAllByOrderByScheduledAtAsc()).thenReturn(List.of(a, b));

        InspectionPlannerResponse response = service().plan();

        assertThat(response.coordinationOpportunities()).singleElement().satisfies(opportunity -> {
            assertThat(opportunity.businessProfileId()).isEqualTo(businessId);
            assertThat(opportunity.inspectionCount()).isEqualTo(2);
            assertThat(opportunity.authorities()).containsExactly("MPCB", "Fire");
        });
    }

    @Test
    void plannerDoesNotCreateOpportunityForSameAuthority() {
        UUID businessId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Inspection a = inspectionForBusiness(businessId, start, "MPCB", "Officer A");
        Inspection b = inspectionForBusiness(businessId, start.plusDays(1), "MPCB", "Officer B");
        when(inspections.findAllByOrderByScheduledAtAsc()).thenReturn(List.of(a, b));

        InspectionPlannerResponse response = service().plan();

        assertThat(response.coordinationOpportunities()).isEmpty();
    }

    @Test
    void plannerIgnoresCompletedHistoricalInspections() {
        Inspection past = historicalInspection(LocalDateTime.now().minusDays(2));
        when(inspections.findAllByOrderByScheduledAtAsc()).thenReturn(List.of(past));

        InspectionPlannerResponse response = service().plan();

        assertThat(response.inspections()).isEmpty();
        assertThat(response.coordinationOpportunities()).isEmpty();
    }

    private InspectionPlannerService service() {
        return new InspectionPlannerService(inspections, 3);
    }

    private Inspection historicalInspection(LocalDateTime scheduledAt) {
        Inspection inspection = mock(Inspection.class);
        when(inspection.getScheduledAt()).thenReturn(scheduledAt);
        return inspection;
    }

    private Inspection inspection(LocalDateTime scheduledAt, String authority, String officer) {
        return inspectionForBusiness(UUID.randomUUID(), scheduledAt, authority, officer);
    }

    private Inspection inspectionForBusiness(UUID businessId, LocalDateTime scheduledAt, String authority, String officer) {
        BusinessProfile profile = mock(BusinessProfile.class);
        when(profile.getId()).thenReturn(businessId);
        when(profile.getBusinessName()).thenReturn("Demo Unit");
        when(profile.getDistrict()).thenReturn("Pune");

        ApplicationRecord app = mock(ApplicationRecord.class);
        when(app.getId()).thenReturn(UUID.randomUUID());
        when(app.getBusinessProfile()).thenReturn(profile);
        when(app.getAuthoritySnapshot()).thenReturn(authority);
        when(app.getExternalReference()).thenReturn("APP-" + UUID.randomUUID());

        Inspection inspection = mock(Inspection.class);
        when(inspection.getId()).thenReturn(UUID.randomUUID());
        when(inspection.getApplication()).thenReturn(app);
        when(inspection.getScheduledAt()).thenReturn(scheduledAt);
        when(inspection.getAssignedOfficer()).thenReturn(officer);
        when(inspection.getOutcome()).thenReturn(InspectionOutcome.PENDING);
        return inspection;
    }
}
