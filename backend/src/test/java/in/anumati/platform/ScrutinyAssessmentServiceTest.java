package in.anumati.platform;

import in.anumati.platform.analysis.ScrutinyAssessmentService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessStage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScrutinyAssessmentServiceTest {
    @Test
    void setupProjectWithEnvironmentalFlagIsEnhanced() {
        ScrutinyAssessmentService service = new ScrutinyAssessmentService(new BigDecimal("1000000000"), 250);
        BusinessProfile profile = new BusinessProfile("Demo", "MANUFACTURING", "PROCESSING", "PUNE", false,
                new BigDecimal("20000000"), null, null, 40,
                Map.of("environmentalConsentRequired", "true"), new BigDecimal("120"), BusinessStage.SETUP, "test");
        var result = service.assess(profile);
        assertThat(result.tier()).isEqualTo("ENHANCED");
        assertThat(result.reasons()).anyMatch(r -> r.contains("environmental consent"));
    }

    @Test
    void hazardousEnvironmentalProjectCanReachHigh() {
        ScrutinyAssessmentService service = new ScrutinyAssessmentService(new BigDecimal("1000000000"), 250);
        BusinessProfile profile = new BusinessProfile("Demo", "MANUFACTURING", "CHEMICAL_PROCESS", "PUNE", false,
                new BigDecimal("2000000000"), null, null, 300,
                Map.of("environmentalConsentRequired", "true", "hazardousProcess", "true"), new BigDecimal("500"), BusinessStage.EXPANSION, "test");
        assertThat(service.assess(profile).tier()).isEqualTo("HIGH");
    }

    @Test
    void ordinaryProfileRemainsStandard() {
        ScrutinyAssessmentService service = new ScrutinyAssessmentService(new BigDecimal("1000000000"), 250);
        BusinessProfile profile = new BusinessProfile("Demo", "SERVICES", "OFFICE", "PUNE", false,
                new BigDecimal("5000000"), null, null, 20, Map.of(), new BigDecimal("25"), BusinessStage.IDEA, "test");
        assertThat(service.assess(profile).tier()).isEqualTo("STANDARD");
    }
}
