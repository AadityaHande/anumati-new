package in.anumati.platform;

import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessStage;
import in.anumati.platform.document.Document;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentProfileVersionTest {
    @Test
    void readyDocumentIsPinnedToTheProfileVersionUsedForValidation() {
        BusinessProfile profile = new BusinessProfile(
                "Example", "GENERIC", "PROCESS", "PUNE", false,
                new BigDecimal("100000"), null, null, 10, java.util.Map.of(), new BigDecimal("25"),
                BusinessStage.SETUP, "applicant");

        Document document = new Document(profile, "PAN", "pan.pdf", "application/pdf", 100, "business/test/pan.pdf");
        document.markReady(profile.getVersionNumber());
        assertThat(document.getValidatedProfileVersion()).isEqualTo(1L);

        profile.update("Example Updated", "GENERIC", "PROCESS", "PUNE", false,
                new BigDecimal("100000"), null, null, 10, java.util.Map.of(), new BigDecimal("25"), BusinessStage.SETUP);

        assertThat(document.getValidatedProfileVersion()).isNotEqualTo(profile.getVersionNumber());
    }
}
