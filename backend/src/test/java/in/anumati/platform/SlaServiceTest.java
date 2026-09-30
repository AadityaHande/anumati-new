package in.anumati.platform;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessStage;
import in.anumati.platform.regulatory.*;
import in.anumati.platform.sla.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {
    @Mock ApprovalSlaConfigRepository configs;
    @Mock ApplicationRepository applications;
    @Mock ApprovalRepository approvals;
    @Mock RegulatorySourceRepository sources;
    @Mock AuditService audit;

    @Test
    void scopedGreenSlaAppliesOnlyToMatchingProfile() {
        BusinessProfile greenProfile = profile(Map.of("pollutionCategory", "GREEN"));
        ApplicationRecord greenApplication = application(greenProfile);
        Approval approval = greenApplication.getApproval();
        RegulatorySource source = verifiedSource();
        ApprovalSlaConfig config = new ApprovalSlaConfig(approval, 720, 168, source, true, Map.of("pollutionCategory", "GREEN"));
        when(configs.findByApproval_Id(approval.getId())).thenReturn(Optional.of(config));

        SlaResponse result = new SlaService(configs, applications, approvals, sources, audit).status(greenApplication);

        assertThat(result.status()).isIn(SlaStatus.ON_TRACK, SlaStatus.AT_RISK, SlaStatus.OVERDUE);
        assertThat(result.targetHours()).isEqualTo(720);
    }

    @Test
    void scopedGreenSlaDoesNotApplyToOrangeProfile() {
        BusinessProfile orangeProfile = profile(Map.of("pollutionCategory", "ORANGE"));
        ApplicationRecord orangeApplication = application(orangeProfile);
        Approval approval = orangeApplication.getApproval();
        RegulatorySource source = verifiedSource();
        ApprovalSlaConfig config = new ApprovalSlaConfig(approval, 720, 168, source, true, Map.of("pollutionCategory", "GREEN"));
        when(configs.findByApproval_Id(approval.getId())).thenReturn(Optional.of(config));

        SlaResponse result = new SlaService(configs, applications, approvals, sources, audit).status(orangeApplication);

        assertThat(result.status()).isEqualTo(SlaStatus.NOT_CONFIGURED);
    }

    private BusinessProfile profile(Map<String, String> attrs) {
        return new BusinessProfile("Demo", "MANUFACTURING", "PROCESSING", "PUNE", false, new BigDecimal("20000000"), null, null, 50, attrs, new BigDecimal("250"), BusinessStage.SETUP, "test");
    }

    private ApplicationRecord application(BusinessProfile profile) {
        RegulatorySource source = verifiedSource();
        Approval approval = new Approval("TEST-SLA-" + UUID.randomUUID(), "Test Approval", "Authority", "Test", source, true);
        return new ApplicationRecord(profile, approval, UUID.randomUUID(), profile.getVersionNumber(), approval.getCode(), approval.getName(), approval.getAuthority(), null, source.getId(), "REF-" + UUID.randomUUID());
    }

    private RegulatorySource verifiedSource() {
        return new RegulatorySource("Source", "https://example.gov.in", SourceType.RULE, SourceVerificationStatus.VERIFIED, null, LocalDate.now().minusDays(1), null, Instant.now(), "hash");
    }
}
